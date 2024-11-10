package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.JsonRecord;
import com.github.kjetilv.uplift.uuid.Uuid;

import java.io.InputStream;
import java.util.Map;

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
