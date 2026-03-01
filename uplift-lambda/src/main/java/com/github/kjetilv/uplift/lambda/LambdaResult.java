package com.github.kjetilv.uplift.lambda;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.kjetilv.uplift.lambda.Utils.encodeResponseBody;
import static com.github.kjetilv.uplift.lambda.Utils.printBody;
import static java.nio.charset.StandardCharsets.UTF_8;

public record LambdaResult(
    int statusCode,
    Map<String, String> headers,
    byte[] body,
    boolean binary
) {

    public static LambdaResult internalError() {
        return new LambdaResult(500, null, null, false);
    }

    @SafeVarargs
    public static LambdaResult json(String body, Map.Entry<String, String>... headers) {
        return json(0, body, headers);
    }

    @SafeVarargs
    public static LambdaResult json(int status, String body, Map.Entry<String, String>... headers) {
        return json(status, body == null ? NONE : body.getBytes(UTF_8), headers);
    }

    @SafeVarargs
    public static LambdaResult json(int status, byte[] body, Map.Entry<String, String>... headers) {
        return new LambdaResult(status, Map.ofEntries(headers), body, false);
    }

    @SafeVarargs
    public static LambdaResult binary(int status, byte[] body, Map.Entry<String, String>... headers) {
        return new LambdaResult(
            status,
            Map.ofEntries(headers),
            body,
            true
        );
    }

    @SafeVarargs
    public static LambdaResult string(int status, String body, Map.Entry<String, String>... headers) {
        return new LambdaResult(
            status,
            Map.ofEntries(headers),
            body.getBytes(UTF_8),
            true
        );
    }

    public static LambdaResult status(int statusCode) {
        return new LambdaResult(
            statusCode,
            null,
            null,
            false
        );
    }

    public LambdaResult(int statusCode, Map<String, String> headers, byte[] body, boolean binary) {
        this.statusCode = statusCode > 0 ? statusCode : 200;
        this.headers = headers == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body = body == null ? NONE : body;
        this.binary = binary;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("statusCode", statusCode());
        var body = body();
        if (body != null && body.length > 0) {
            map.put("isBase64Encoded", this.binary());
            map.put("body", encodeResponseBody(body, this.binary()));
        }
        map.put("headers", headers());
        return map;
    }

    private static final byte[] NONE = new byte[0];

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + statusCode + ": " + printBody(body, binary) + "]";
    }
}
