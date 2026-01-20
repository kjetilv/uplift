package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.lambda.ResponseIn;
import com.github.kjetilv.uplift.lambda.ResponseInRW;
import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.HttpMethod;
import com.github.kjetilv.uplift.synchttp.Server;
import com.github.kjetilv.uplift.synchttp.req.HttpReq;
import com.github.kjetilv.uplift.util.RuntimeCloseable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.StructuredTaskScope.Joiner.allSuccessfulOrThrow;

final class Flambda implements RuntimeCloseable {

    private static final Logger log = LoggerFactory.getLogger(Flambda.class);

    private final FlambdaState flambdaState;

    private final Server lambdaServer;

    private final Server apiServer;

    Flambda(FlambdaSettings settings) {
        this.flambdaState = new FlambdaState(
            Objects.requireNonNull(settings, "settings").queueLength());

        var apiServerHandler = new HttpCallbackProcessor(apiHandler());
        var apiPort = settings.apiPort();
        this.apiServer = Server.create(apiPort).run(apiServerHandler);

        var lambdaServerHandler = new HttpCallbackProcessor(lambdaHandler());
        var lambdaPort = settings.lambdaPort();
        this.lambdaServer = Server.create(lambdaPort).run(lambdaServerHandler);
    }

    @Override
    public void close() {
        try (var scope = StructuredTaskScope.open(allSuccessfulOrThrow())) {
            scope.fork(lambdaServer::close);
            scope.fork(apiServer::close);
            scope.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        }
    }

    URI lambdaUri() {
        return uri(lambdaServer.address());
    }

    URI apiUri() {
        return uri(apiServer.address());
    }

    InetSocketAddress lambda() {
        return lambdaServer.address();
    }

    InetSocketAddress api() {
        return apiServer.address();
    }

    private HttpCallbackProcessor.HttpHandler apiHandler() {
        return (httpReq, callback) -> {
            var method = httpReq.method();
            var requestOut = requestOut(httpReq, method);
            flambdaState.exchange(
                new LambdaReq(requestOut),
                lambdaRes -> {
                    var body = lambdaRes.in().body();
                    var in = lambdaRes.in();
                    callback.status(in.statusCode())
                        .headers(in.headers())
                        .contentLength(in.body().length())
                        .body(body);
                }
            );
        };
    }

    private HttpCallbackProcessor.HttpHandler lambdaHandler() {
        return (httpReq, callback) -> {
            switch (httpReq.method()) {
                case GET -> {
                    var lambdaReq = flambdaState.fetchRequest();
                    var body = lambdaReq.out().body();
                    callback.status(200)
                        .headers(idHeaders(lambdaReq))
                        .contentLength(length(body))
                        .body(body);
                }
                case POST -> {
                    var id = id(httpReq.path());
                    var body = httpReq.bodyBytes();
                    flambdaState.submitResponse(new LambdaRes(id, responseIn(body)));
                    callback.status(204).nobody();
                }
                default -> log.error("Unsupported method: {}", httpReq);
            }
        };
    }

    private static int length(String body) {
        return body == null || body.isEmpty() ? 0 : body.length();
    }

    private static @NonNull Map<String, Object> idHeaders(LambdaReq lambdaReq) {
        return Map.of(
            "lambda-runtime-aws-request-id", lambdaReq.id().digest(),
            "content-type", "application/json"
        );
    }

    private static ResponseIn responseIn(byte[] body) {
        return ResponseInRW.INSTANCE.bytesReader().read(body);
    }

    private static RequestOut requestOut(
        HttpReq httpReq,
        HttpMethod method
    ) {
        return new RequestOut(
            method.name(),
            httpReq.path(),
            httpReq.headerMap(),
            httpReq.queryParametersMap(),
            switch (method) {
                case GET, HEAD, OPTIONS, DELETE -> null;
                default -> httpReq.bodyString(UTF_8);
            }
        );
    }

    private static URI uri(InetSocketAddress address) {
        return URI.create("http://%s:%d".formatted(address.getHostString(), address.getPort()));
    }

    private static Hash<HashKind.K128> id(String path) {
        try {
            var split = path.split("/");
            for (int i = 0; i < split.length; i++) {
                if (split[i] == null || split[i].isBlank()) {
                    continue;
                }
                if (split[i].charAt(0) == 'i' && split[i].equals("invocation")) {
                    if (i + 1 < split.length) {
                        var id = split[i + 1];
                        return Hash.from(id);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse id from " + path, e);
        }
        throw new IllegalStateException("Failed to parse id from " + path);
    }
}
