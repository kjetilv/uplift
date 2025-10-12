package com.github.kjetilv.uplift.flambda;

import module java.base;
import module uplift.asynchttp;

/// Emulates AWS lambda service.
@SuppressWarnings("unused")
public final class LocalLambda implements Closeable, Runnable, HttpChannelHandler.Server {

    private final IOServer lambdaServer;

    private final LocalLambdaHandler lambdaHandler;

    private final IOServer apiServer;

    private final URI lambdaUri;

    private final URI apiUri;

    public LocalLambda(LocalLambdaSettings settings) {
        this.lambdaHandler = new LocalLambdaHandler(settings.queueLength());

        var lambdaServerRunner = ServerRunner.create(
            settings.lambdaPort(),
            settings.requestBufferSize()
        );
        this.lambdaServer = lambdaServerRunner.run(new HttpChannelHandler(
            lambdaHandler,
            settings.requestBufferSize(),
            settings.time()
        ));

        var apiServerRunner = ServerRunner.create(
            settings.apiPort(),
            settings.requestBufferSize()
        );
        this.apiServer = apiServerRunner.run(new HttpChannelHandler(
            new LocalApiHandler(lambdaHandler, settings.cors()),
            settings.requestBufferSize(),
            settings.time()
        ));

        var lambdaAddress = lambdaServer.address();
        this.lambdaUri = URI.create(String.format(URL, lambdaAddress.getPort()));

        var apiAddress = this.apiServer.address();
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
        lambdaServer.join();
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

    void awaitStarted(Duration timeout) {
        lambdaServer.awaitActive(timeout);
    }

    private static final String URL = "http://localhost:%1$d";

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[api@" + apiUri + " -> lambda@" + lambdaUri + "]";
    }
}
