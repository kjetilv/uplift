package com.github.kjetilv.uplift.flambda;

import module java.base;
import module uplift.asynchttp;
import module uplift.kernel;
import module uplift.util;
import module uplift.uuid;

record LambdaResponse(
    int statusCode,
    Map<String, String> headers,
    String body,
    boolean isBase64Encoded,
    Uuid reqId
) {

    HttpRes toHttpResponse() {
        return new HttpRes(
            statusCode(),
            Maps.mapValues(headers(), Collections::singletonList),
            resolveBody(),
            reqId()
        );
    }

    private byte[] resolveBody() {
        return body == null || body.isEmpty() ? NO_BODY
            : isBase64Encoded() ? BytesIO.fromBase64(body())
                : body().getBytes(StandardCharsets.UTF_8);
    }

    public static final byte[] NO_BODY = {};
}
