package com.github.kjetilv.uplift.flambda;

import module java.base;
import module java.net.http;
import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.HttpHandler;
import com.github.kjetilv.uplift.synchttp.Server;
import com.github.kjetilv.uplift.synchttp.Servers;
import com.github.kjetilv.uplift.util.RuntimeCloseable;
import com.github.kjetilv.uplift.util.Virtuals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Flambda implements RuntimeCloseable, Runnable {

    private static final Logger log = LoggerFactory.getLogger(Flambda.class);

    private final Server.Open lambdaServer;

    private final Server.Open apiServer;

    private final String name;

    public Flambda(FlambdaSettings settings) {
        this.name = Objects.requireNonNull(settings, "settings").name();

        var flambdaState = new FlambdaState(settings.queueLength());

        HttpHandler apiHandler = new ApiHandler(settings, flambdaState);
        HttpHandler flambdaHandler = new FlambdaHandler(settings, flambdaState);

        this.apiServer = openServer(settings.apiPort(), apiHandler);
        this.lambdaServer = openServer(settings.lambdaPort(), flambdaHandler);

        Runtime.getRuntime().addShutdownHook(Virtuals.thread(
            Flambda.class.getSimpleName().toLowerCase(Locale.ROOT) + "-shutdown",
            () -> {
                log.info("Shutting down {}", this);
                close();
            }
        ));
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

    private void await(Function<Server.Open, Runnable> task) {
        try (var executor = Virtuals.executor("flambda")) {
            CompletableFuture<Void> lambdaFuture = CompletableFuture.runAsync(task.apply(lambdaServer), executor);
            CompletableFuture<Void> apiFuture = CompletableFuture.runAsync(task.apply(apiServer), executor);
            lambdaFuture.join();
            apiFuture.join();
        }
    }

    private static Server.Open openServer(Integer port, HttpHandler apiHandler) {
        return Servers.create(port)
            .open(new HttpCallbackProcessor(apiHandler));
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
