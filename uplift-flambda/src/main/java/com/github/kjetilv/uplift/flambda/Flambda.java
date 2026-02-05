package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.lambda.RequestOutRW;
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
import java.util.function.Function;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.StructuredTaskScope.Joiner.allSuccessfulOrThrow;

public final class Flambda implements RuntimeCloseable, Runnable {

    private static final Logger log = LoggerFactory.getLogger(Flambda.class);

    private final Server lambdaServer;

    private final Server apiServer;

    public Flambda(FlambdaSettings settings) {
        Objects.requireNonNull(settings, "settings");

        var flambdaState = new FlambdaState(settings.queueLength());

        this.apiServer = Server.create(settings.apiPort())
            .run(new HttpCallbackProcessor(apiHandler(flambdaState)));

        this.lambdaServer = Server.create(settings.lambdaPort())
            .run(new HttpCallbackProcessor(lambdaHandler(flambdaState)));
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

    private static @NonNull StructuredTaskScope<Object, Stream<StructuredTaskScope.Subtask<Object>>> newScope() {
        return StructuredTaskScope.open(allSuccessfulOrThrow());
    }

    private static HttpCallbackProcessor.HttpHandler lambdaHandler(FlambdaState flambdaState) {
        return (httpReq, callback) -> {
            switch (httpReq.method()) {
                case GET -> {
                    var lambdaReq = flambdaState.fetchRequest();
                    var body = RequestOutRW.INSTANCE.stringWriter().write(lambdaReq.out());
                    var contentLength = length(body);
                    callback.status(200)
                        .headers(idHeaders(lambdaReq))
                        .contentLength(contentLength)
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

    private static HttpCallbackProcessor.HttpHandler apiHandler(FlambdaState flambdaState) {
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

    @SuppressWarnings("HttpUrlsUsage")
    private static URI uri(InetSocketAddress address) {
        return URI.create("http://%s:%d".formatted(address.getHostString(), address.getPort()));
    }

    private static Hash<HashKind.K128> id(String path) {
        try {
            var split = path.split("/");
            for (var i = 0; i < split.length; i++) {
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + lambdaServer + " <=> " + apiServer + "]";
    }
}
