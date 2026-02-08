package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.io.InputStream;
import java.util.Map;

@JsonRecord
public record ResponseIn(
    int statusCode,
    Map<String, Object> headers,
    String body,
    boolean isBase64Encoded,
    String reqId
) {

    public static ResponseIn read(InputStream bytes) {
        return ResponseInRW.INSTANCE.streamReader().read(bytes);
    }

    public long bodyLength() {
        return body == null ? 0 : body.length();
    }
}
