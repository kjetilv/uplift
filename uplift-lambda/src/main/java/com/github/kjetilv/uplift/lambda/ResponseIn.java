package com.github.kjetilv.uplift.lambda;

import module java.base;
import module uplift.json.anno;
import module uplift.uuid;

@JsonRecord
public record ResponseIn(
    int statusCode,
    Map<String, Object> headers,
    String body,
    boolean isBase64Encoded,
    Uuid reqId
) {

    public static ResponseIn read(InputStream bytes) {
        return ResponseInRW.INSTANCE.streamReader().read(bytes);
    }
}
