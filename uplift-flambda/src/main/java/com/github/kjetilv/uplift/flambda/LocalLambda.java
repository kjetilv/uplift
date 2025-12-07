package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.asynchttp.HttpAsyncChannelHandler;
import com.github.kjetilv.uplift.asynchttp.HttpReq;
import com.github.kjetilv.uplift.asynchttp.HttpRes;
import com.github.kjetilv.uplift.asynchttp.AsyncIOServer;
import com.github.kjetilv.uplift.asynchttp.AsyncServerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Emulates AWS lambda service.
@SuppressWarnings("unused")
public final class LocalLambda implements Closeable, Runnable, HttpAsyncChannelHandler.Server {

    public static final Logger LOG = LoggerFactory.getLogger(LocalLambda.class);

    private final AsyncIOServer lambdaServer;

    private final LocalLambdaHandler lambdaHandler;

    private final AsyncIOServer apiServer;

    private final URI lambdaUri;

    private final URI apiUri;

    public LocalLambda(LocalLambdaSettings settings) {
        this.lambdaHandler = new LocalLambdaHandler(settings);

        var lambdaServerRunner = AsyncServerRunner.create(
            settings.lambdaPort(),
            settings.requestBufferSize()
        );

        var lambdaHandler = new HttpAsyncChannelHandler(
            this.lambdaHandler,
            settings.requestBufferSize(),
            settings.time()
        );

        this.lambdaServer = lambdaServerRunner.run(lambdaHandler);

        var apiServerRunner = AsyncServerRunner.create(
            settings.apiPort(),
            settings.requestBufferSize()
        );
        var apiHandler = new HttpAsyncChannelHandler(
            new LocalApiHandler(this.lambdaHandler, settings.cors()),
            settings.requestBufferSize(),
            settings.time()
        );
        this.apiServer = apiServerRunner.run(apiHandler);

        var lambdaPort = lambdaServer.port();
        this.lambdaUri = URI.create(String.format(URL, lambdaPort));
        LOG.info("Lambda : {} @ {}", lambdaServer, lambdaUri);

        var apiPort = this.apiServer.port();
        this.apiUri = URI.create(String.format(URL, apiPort));
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
