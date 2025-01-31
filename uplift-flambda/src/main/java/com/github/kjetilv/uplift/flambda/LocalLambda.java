package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.asynchttp.*;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Emulates AWS lambda service.
 */
@SuppressWarnings("unused")
public final class LocalLambda implements Closeable, Runnable, HttpChannelHandler.Server {

    private final IOServer lambdaServer;

    private final LocalLambdaHandler lambdaHandler;

    private final IOServer apiServer;

    private final URI lambdaUri;

    private final URI apiUri;

    public LocalLambda(
        LocalLambdaSettings settings,
        ExecutorService lambdaExecutor,
        ExecutorService serverExecutor
    ) {
        this.lambdaHandler = new LocalLambdaHandler(settings.queueLength());

        ServerRunner lambdaServerRunner = ServerRunner.create(
            settings.lambdaPort(),
            settings.requestBufferSize(),
            Objects.requireNonNull(lambdaExecutor, "lambdaExecutor")
        );
        this.lambdaServer = lambdaServerRunner.run(new HttpChannelHandler(
            lambdaHandler,
            settings.requestBufferSize(),
            settings.time()
        ));

        ServerRunner apiServerRunner = ServerRunner.create(
            settings.apiPort(),
            settings.requestBufferSize(),
            Objects.requireNonNull(serverExecutor, "serverExecutor")
        );
        this.apiServer = apiServerRunner.run(new HttpChannelHandler(
            new LocalApiHandler(lambdaHandler, settings.cors()),
            settings.requestBufferSize(),
            settings.time()
        ));

        InetSocketAddress lambdaAddress = lambdaServer.address();
        this.lambdaUri = URI.create(String.format(URL, lambdaAddress.getPort()));

        InetSocketAddress apiAddress = this.apiServer.address();
        this.apiUri = URI.create(String.format(URL, apiAddress.getPort()));
    }

    @Override
    public void close() {
        lambdaHandler.close();

        lambdaServer.close();
        lambdaServer.join();

        apiServer.close();
        apiServer.join();
    }

    @Override
    public void run() {
        join();
    }

    @Override
    public HttpRes handle(HttpReq req) {
        return lambdaHandler.lambdaResponse(new LambdaRequest(req)).toHttpResponse();
    }

    public Reqs reqs() {
        return new ReqsImpl(getApiUri());
    }

    public URI getLambdaUri() {
        return lambdaUri;
    }

    public URI getApiUri() {
        return apiUri;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[api@" + apiUri + " -> lambda@" + lambdaUri + "]";
    }

    void join() {
        lambdaServer.join();
    }

    void awaitStarted(Duration timeout) {
        lambdaServer.awaitActive(timeout);
    }

    private static final String URL = "http://localhost:%1$d";
}
