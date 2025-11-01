package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.asynchttp.HttpRes;
import com.github.kjetilv.uplift.kernel.io.BytesIO;
import com.github.kjetilv.uplift.util.Maps;
import com.github.kjetilv.uplift.uuid.Uuid;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

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
