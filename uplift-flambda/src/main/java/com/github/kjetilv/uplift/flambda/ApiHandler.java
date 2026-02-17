package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.synchttp.HttpHandler;
import com.github.kjetilv.uplift.synchttp.HttpMethod;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;

import static java.nio.charset.StandardCharsets.UTF_8;

record ApiHandler(
    FlambdaSettings settings,
    FlambdaState flambdaState
) implements HttpHandler {

    @Override
    public void handle(HttpReq httpReq, HttpResponseCallback callback) {
        switch (httpReq.method()) {
            case OPTIONS -> {
                settings.cors().applyTo(
                    httpReq.origin(),
                    callback.status(200)
                ).nobody();
            }
            case HttpMethod method -> {
                var lambdaReq = new LambdaReq(requestOut(httpReq, method));
                flambdaState.exchange(
                    lambdaReq,
                    lambdaRes ->
                        respond(httpReq, lambdaRes, callback)
                );
            }
        }
    }

    private void respond(
        HttpReq httpReq,
        LambdaRes lambdaRes,
        HttpResponseCallback callback
    ) {
        var in = lambdaRes.in();
        var origin = httpReq.origin();
        var headers = settings.cors()
            .applyTo(origin, callback.status(in.statusCode()))
            .headers(in.headers());
        byte[] bodyBytes = in.bytes();
        if (bodyBytes.length == 0) {
            headers.nobody();
        } else {
            headers
                .contentLength(bodyBytes.length)
                .body(bodyBytes);
        }
    }

    private static RequestOut requestOut(HttpReq httpReq, HttpMethod method) {
        var req = httpReq.withQueryParameters();
        return new RequestOut(
            method.name(),
            req.path(),
            req.headerMap(),
            req.queryParametersMap(),
            switch (method) {
                case GET, HEAD, OPTIONS -> null;
                default -> req.bodyString(UTF_8);
            }
        );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings + " " + flambdaState + "]";
    }
}
