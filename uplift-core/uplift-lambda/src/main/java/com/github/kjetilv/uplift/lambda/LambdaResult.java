package com.github.kjetilv.uplift.lambda;

import java.util.HashMap;
import java.util.Map;

import static com.github.kjetilv.uplift.lambda.Utils.encodeResponseBody;
import static com.github.kjetilv.uplift.lambda.Utils.printBody;

public record LambdaResult(
    int statusCode,
    Map<String, String> headers,
    byte[] body,
    boolean binary
) {

    @SafeVarargs
    public static LambdaResult json(int status, byte[] body, Map.Entry<String, String>... headers) {
        return new LambdaResult(status, Map.ofEntries(headers), body, false);
    }

    @SafeVarargs
    public static LambdaResult binary(int status, byte[] body, Map.Entry<String, String>... headers) {
        return new LambdaResult(status, Map.ofEntries(headers), body, true);
    }

    public static LambdaResult status(int statusCode) {
        return new LambdaResult(statusCode, null, null, false);
    }

    public Map<String, Object> toMap(boolean post) {
        Map<String, Object> map = new HashMap<>();
        map.put("statusCode", statusCode());
        byte[] body = body();
        if (body != null && body.length > 0) {
            map.put("isBase64Encoded", this.binary());
            map.put("body", encodeResponseBody(body, this.binary()));
        }
        map.put("headers", headers());
        return map;
    }

    private static Map<String, String> newCorsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache");
        return headers;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + statusCode + ": " + printBody(body, binary) + "]";
    }
}
