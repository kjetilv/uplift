package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.Server;
import com.github.kjetilv.uplift.util.RuntimeCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.concurrent.StructuredTaskScope.Joiner.allSuccessfulOrThrow;

public final class Flambda implements RuntimeCloseable, Runnable {

    private static final Logger log = LoggerFactory.getLogger(Flambda.class);

    private final Server lambdaServer;

    private final Server apiServer;

    private final String name;

    public Flambda(FlambdaSettings settings) {
        this.name = Objects.requireNonNull(settings, "settings").name();

        var flambdaState = new FlambdaState(settings.queueLength());

        this.apiServer = Server.create(settings.apiPort())
            .run(new HttpCallbackProcessor(new ApiHandler(settings, flambdaState)));

        this.lambdaServer = Server.create(settings.lambdaPort())
            .run(new HttpCallbackProcessor(new LambdaHandler(settings, flambdaState)));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down {}", this);
            close();
        }));
        log.info("{} started", this);
    }

    public void join() {
        await(server -> server::join);
    }

    @Override
    public void run() {
        join();
    }

    @Override
    public void close() {
        await(server -> server::close);
    }

    public URI lambdaUri() {
        return uri(lambdaServer.address());
    }

    public URI apiUri() {
        return uri(apiServer.address());
    }

    public InetSocketAddress lambda() {
        return lambdaServer.address();
    }

    public InetSocketAddress api() {
        return apiServer.address();
    }

    public Reqs reqs() {
        return new ReqsImpl(apiUri());
    }

    private void await(Function<Server, Runnable> task) {
        try (var scope = newScope()) {
            scope.fork(task.apply(lambdaServer));
            scope.fork(task.apply(apiServer));
            scope.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        }

    }

    private StructuredTaskScope<Object, Stream<StructuredTaskScope.Subtask<Object>>> newScope() {
        return StructuredTaskScope.open(
        allSuccessfulOrThrow(), configuration -> configuration
                .withThreadFactory(Thread.ofVirtual().factory())
                .withName(name)
                .withTimeout(Duration.ofMinutes(1))
        );
    }

    @SuppressWarnings("HttpUrlsUsage")
    private static URI uri(InetSocketAddress address) {
        return URI.create("http://%s:%d".formatted(address.getHostString(), address.getPort()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ": " + lambdaServer + " <=> " + apiServer + "]";
    }
}
