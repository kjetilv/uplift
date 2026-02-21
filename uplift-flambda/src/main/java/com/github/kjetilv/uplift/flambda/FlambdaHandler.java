package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.lambda.RequestOutRW;
import com.github.kjetilv.uplift.lambda.ResponseIn;
import com.github.kjetilv.uplift.lambda.ResponseInRW;
import com.github.kjetilv.uplift.synchttp.HttpHandler;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

record FlambdaHandler(
    FlambdaSettings settings,
    FlambdaState flambdaState
) implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(FlambdaHandler.class);

    @Override
    public void handle(HttpReq httpReq, HttpResponseCallback callback) {
        switch (httpReq.method()) {
            case GET -> {
                var lambdaReq = flambdaState.fetchRequest();
                var body = toString(lambdaReq.out());
                var contentLength = length(body);
                callback.status(200)
                    .headers(idHeaders(lambdaReq))
                    .contentLength(contentLength)
                    .body(body);
            }
            case POST -> {
                var id = id(httpReq.path());
                var body = httpReq.bodyBytes();
                var lambdaRes = new LambdaRes(id, responseIn(body));
                flambdaState.submitResponse(lambdaRes);
                callback.status(204).nobody();
            }
            case OPTIONS -> settings.cors().applyTo(httpReq.origin(), callback.status(200));
            default -> log.error("Unsupported method: {}", httpReq);
        }
    }

    private static ResponseIn responseIn(byte[] body) {
        return ResponseInRW.INSTANCE.bytesReader().read(body);
    }

    private static String toString(RequestOut request) {
        return RequestOutRW.INSTANCE.stringWriter().write(request);
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

    private static @NonNull Map<String, Object> idHeaders(LambdaReq lambdaReq) {
        return Map.of(
            "lambda-runtime-aws-request-id", lambdaReq.id().digest(),
            "content-type", "application/json"
        );
    }

    private static int length(String body) {
        return body == null || body.isEmpty() ? 0 : body.length();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings + " " + flambdaState + "]";
    }
}
