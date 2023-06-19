package com.github.kjetilv.uplift.flambda;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.github.kjetilv.uplift.asynchttp.HttpRes;
import com.github.kjetilv.uplift.kernel.io.BytesIO;

record LambdaResponse(
    int statusCode,
    Map<String, List<String>> headers,
    String body,
    boolean isBase64Encoded
) {

    HttpRes toHttpResponse() {
        return new HttpRes(statusCode(), headers(), resolveBody());
    }

    private byte[] resolveBody() {
        return body == null || body.isEmpty() ? BytesIO.NOBODY
            : isBase64Encoded() ? BytesIO.fromBase64(body())
                : body().getBytes(StandardCharsets.UTF_8);
    }
}
