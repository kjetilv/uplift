package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.uuid.Uuid;

import java.util.Map;

@JsonRecord
public record ResponseIn(
    int statusCode,
    Map<String, Object> headers,
    String body,
    boolean isBase64Encoded,
    Uuid reqId
) {
}
