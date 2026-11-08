package com.github.kjetilv.uplift.flambda;

import module java.base;
import module java.net.http;
import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.HttpHandler;
import com.github.kjetilv.uplift.synchttp.Server;
import com.github.kjetilv.uplift.synchttp.ServerSettings;
import com.github.kjetilv.uplift.synchttp.Servers;
import com.github.kjetilv.uplift.util.RuntimeCloseable;
import com.github.kjetilv.uplift.util.Virtuals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public final class Flambda implements RuntimeCloseable, Runnable {

    private static final Logger log = LoggerFactory.getLogger(Flambda.class);

    private final Server.Open lambdaServer;

    private final Server.Open apiServer;

    private final String name;

    private final FlambdaState flambdaState;

    private final Thread shutdownHook;

    private final AtomicBoolean closed = new AtomicBoolean();

    public Flambda(FlambdaSettings settings) {
        this.name = Objects.requireNonNull(settings, "settings").name();

        this.flambdaState = new FlambdaState(settings.queueLength());

        HttpHandler apiHandler = new ApiHandler(settings, flambdaState);
        HttpHandler flambdaHandler = new FlambdaHandler(settings, flambdaState);

        this.apiServer = openServer(settings.address(), settings.apiPort(), apiHandler);
        this.lambdaServer = openServer(settings.address(), settings.lambdaPort(), flambdaHandler);

        this.shutdownHook = Virtuals.thread(
            Flambda.class.getSimpleName().toLowerCase(Locale.ROOT) + "-shutdown",
            () -> {
                log.info("Shutting down {}", this);
                close();
            }
        );
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        log.info("{} started", this);
    }

    public void join() {
        await(server -> server::join);
    }

    @Override
    public void run() {
        join();
    }

    /// Ends all waiting requests with 503, releases waiting Lambda polls, and closes both servers.
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            flambdaState.close();
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException e) {
                log.debug("Shutdown in progress: {}", this);
            }
            await(server -> server::close);
        }
    }

    /// @return URI for the next invocation, `.../2018-06-01/runtime/invocation/next`, like
    ///     [com.github.kjetilv.uplift.kernel.Env#awsLambdaUri()]
    public URI lambdaUri() {
        return uri(lambdaServer.address()).resolve(NEXT_INVOCATION);
    }

    /// @return Host and port of the Runtime API, like the `AWS_LAMBDA_RUNTIME_API` environment variable
    public String runtimeApi() {
        var address = lambdaServer.address();
        return address.getHostString() + ":" + address.getPort();
    }

    /// @return Number of API requests not yet ended
    public int pendingRequests() {
        return flambdaState.pending();
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
            var lambdaFuture = CompletableFuture.runAsync(task.apply(lambdaServer), executor);
            var apiFuture = CompletableFuture.runAsync(task.apply(apiServer), executor);
            lambdaFuture.join();
            apiFuture.join();
        }
    }

    private static Server.Open openServer(InetAddress address, Integer port, HttpHandler handler) {
        return Servers.create(new InetSocketAddress(address, port == null ? 0 : port), ServerSettings.DEFAULT)
            .open(new HttpCallbackProcessor(handler));
    }

    private static final String NEXT_INVOCATION = "/2018-06-01/runtime/invocation/next";

    @SuppressWarnings("HttpUrlsUsage")
    private static URI uri(InetSocketAddress address) {
        return URI.create("http://%s:%d".formatted(address.getHostString(), address.getPort()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ": " + lambdaServer + " <=> " + apiServer + "]";
    }
}
