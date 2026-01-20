package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.Server;
import com.github.kjetilv.uplift.synchttp.req.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import com.github.kjetilv.uplift.util.RuntimeCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Emulates AWS lambda service.
@SuppressWarnings("unused")
public final class LocalLambda implements RuntimeCloseable, Runnable, HttpCallbackProcessor.HttpHandler {

    public static final Logger LOG = LoggerFactory.getLogger(LocalLambda.class);

    private final LocalLambdaHandler lambdaHandler;

    private final LocalApiHandler apiHandler;

    private final Server lambdaServer;

    private final Server apiServer;

    private final URI lambdaUri;

    private final URI apiUri;

    public LocalLambda(FlambdaSettings settings) {
        this.lambdaHandler = new LocalLambdaHandler(settings);
        this.apiHandler = new LocalApiHandler(
            lambdaHandler, new CorsSettings(
            List.of("*"),
            List.of("GET", "POST", "PUT", "DELETE"),
            List.of()
        )
        );

        var lambdaServerHandler = new HttpCallbackProcessor(lambdaHandler);
        var apiServerHandler = new HttpCallbackProcessor(apiHandler);

        var apiPort = settings.apiPort();
        var lambdaPort = settings.lambdaPort();

        lambdaServer = Server.create(lambdaPort).run(lambdaServerHandler);
        apiServer = Server.create(apiPort).run(apiServerHandler);

        this.lambdaUri = lambdaServer.uri();
        this.apiUri = apiServer.uri();

        LOG.debug("API @ {}", apiUri);
        LOG.debug("Lambda @ {}", lambdaUri);
    }

    @Override
    public void close() {
        lambdaHandler.close();
    }

    @Override
    public void run() {
        lambdaServer.join();
        apiServer.join();
    }

    @Override
    public void handle(HttpReq httpReq, HttpResponseCallback callback) {
        lambdaHandler.lambdaResponse(new LambdaReq(
            new RequestOut(
                httpReq.method().name(),
                httpReq.reqLine().url(),
                httpReq.headerMap(),
                httpReq.queryParameters().toMap(),
                httpReq.bodyString(StandardCharsets.UTF_8)
            )));
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

    private static final String URL = "http://localhost:%1$d";

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[api@" + apiUri + " -> lambda@" + lambdaUri + "]";
    }
}
