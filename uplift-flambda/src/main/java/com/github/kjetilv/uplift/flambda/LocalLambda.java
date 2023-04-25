package com.github.kjetilv.uplift.flambda;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.URI;

import com.github.kjetilv.uplift.asynchttp.ChannelHandler;
import com.github.kjetilv.uplift.asynchttp.HttpChannelHandler;
import com.github.kjetilv.uplift.asynchttp.HttpChannelState;
import com.github.kjetilv.uplift.asynchttp.HttpRequest;
import com.github.kjetilv.uplift.asynchttp.HttpResponse;
import com.github.kjetilv.uplift.asynchttp.IOServer;
import com.github.kjetilv.uplift.asynchttp.ServerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Emulates AWS lambda service.
 */
public final class LocalLambda implements Closeable, Runnable, HttpChannelHandler.Server {

    private static final Logger log = LoggerFactory.getLogger(LocalLambda.class);

    private final IOServer lambdaServer;

    private final LocalLambdaHandler lambdaHandler;

    private final IOServer apiServer;

    private final URI lambdaUri;

    private final URI apiUri;

    public LocalLambda(LocalLambdaSettings lls) {
        this.lambdaHandler = new LocalLambdaHandler(lls.queueLength());

        ChannelHandler<HttpChannelState, HttpChannelHandler> lambdaServiceHandler =
            new HttpChannelHandler(
                lambdaHandler,
                lls.requestBufferSize(),
                lls.time()
            );

        CorsSettings corsSettings = lls.corsSettings();
        ChannelHandler<HttpChannelState, HttpChannelHandler> apiServiceHandler =
            new HttpChannelHandler(
                new LocalApiHandler(lambdaHandler, corsSettings),
                lls.requestBufferSize(),
                lls.time()
            );

        this.lambdaServer = ServerRunner.create(lls.lambdaPort(), lls.requestBufferSize(), lls.lambdaExecutor())
            .run(lambdaServiceHandler);

        this.apiServer = ServerRunner.create(lls.apiPort(), lls.requestBufferSize(), lls.serverExecutor())
            .run(apiServiceHandler);

        InetSocketAddress lambdaAddress = lambdaServer.address();
        this.lambdaUri = URI.create(String.format("http://localhost:%1$d", lambdaAddress.getPort()));

        InetSocketAddress apiAddress = this.apiServer.address();
        this.apiUri = URI.create(String.format("http://localhost:%1$d", apiAddress.getPort()));

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
    public HttpResponse handle(HttpRequest req) {
        LambdaRequest request = new LambdaRequest(req);
        LambdaResponse response = lambdaHandler.lambdaResponse(request);
        return response.toHttpResponse();
    }

    public URI getLambdaUri() {
        return lambdaUri;
    }

    public URI getApiUri() {
        return apiUri;
    }

    void join() {
        lambdaServer.join();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[api@" + apiUri + " -> lambda@" + lambdaUri + "]";
    }
}
