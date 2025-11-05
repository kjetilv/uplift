package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.asynchttp.HttpChannelHandler;
import com.github.kjetilv.uplift.asynchttp.HttpReq;
import com.github.kjetilv.uplift.asynchttp.HttpRes;
import com.github.kjetilv.uplift.asynchttp.IOServer;
import com.github.kjetilv.uplift.asynchttp.ServerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Emulates AWS lambda service.
@SuppressWarnings("unused")
public final class LocalLambda implements Closeable, Runnable, HttpChannelHandler.Server {

    public static final Logger LOG = LoggerFactory.getLogger(LocalLambda.class);

    private final IOServer lambdaServer;

    private final LocalLambdaHandler lambdaHandler;

    private final IOServer apiServer;

    private final URI lambdaUri;

    private final URI apiUri;

    public LocalLambda(LocalLambdaSettings settings) {
        this.lambdaHandler = new LocalLambdaHandler(settings);

        var lambdaServerRunner = ServerRunner.create(
            settings.lambdaPort(),
            settings.requestBufferSize()
        );

        var lambdaHandler = new HttpChannelHandler(
            this.lambdaHandler,
            settings.requestBufferSize(),
            settings.time()
        );

        this.lambdaServer = lambdaServerRunner.run(lambdaHandler);

        var apiServerRunner = ServerRunner.create(
            settings.apiPort(),
            settings.requestBufferSize()
        );
        var apiHandler = new HttpChannelHandler(
            new LocalApiHandler(this.lambdaHandler, settings.cors()),
            settings.requestBufferSize(),
            settings.time()
        );
        this.apiServer = apiServerRunner.run(apiHandler);

        var lambdaAddress = lambdaServer.address();
        this.lambdaUri = URI.create(String.format(URL, lambdaAddress.getPort()));
        LOG.info("Lambda : {} @ {}", lambdaServer, lambdaUri);

        var apiAddress = this.apiServer.address();
        this.apiUri = URI.create(String.format(URL, apiAddress.getPort()));
        LOG.info("API    : {} @ {}", apiServer, apiUri);
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
