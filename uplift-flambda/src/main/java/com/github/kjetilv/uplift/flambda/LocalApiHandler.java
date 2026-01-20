package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.synchttp.HttpCallbackProcessor;
import com.github.kjetilv.uplift.synchttp.req.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;

record LocalApiHandler(LocalLambdaHandler handler, Map<String, Object> corsHeaders)
    implements HttpCallbackProcessor.HttpHandler {

    LocalApiHandler(LocalLambdaHandler handler, CorsSettings corsHeaders) {
        Objects.requireNonNull(corsHeaders, "cors");
        this(
            Objects.requireNonNull(handler, "handler"),
            Map.of(
                "access-control-allow-origin", "*",
                "access-control-allow-methods", corsHeaders.methodsValue(),
                "access-control-allow-headers", corsHeaders.headersValue(),
                "access-control-max-age", "86400",
                "access-control-allow-credentials", corsHeaders.credentialsValue()
            )
        );
    }

    @Override
    public void handle(
        HttpReq httpReq,
        HttpResponseCallback callback
    ) {
        if (httpReq.isCors()) {
            callback.status(200).headers(corsHeaders);
        } else {
            respond(
                callback,
                null
//                handler.lambdaResponse(new LambdaReq(httpReq))
            );
        }
    }

    private void respond(HttpResponseCallback callback, LambdaRes lambdaRes) {
        var in = lambdaRes.in();
        callback.status(in.statusCode())
            .headers(in.headers())
            .headers(corsHeaders)
            .content()
            .body(in.body());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + handler + "]";
    }
}
