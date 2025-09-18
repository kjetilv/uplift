package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.util.Map;

/// Record structure that can capture both 1.0 and 2.0 inputs.
@JsonRecord
public record RequestIn(
    String version,
    String httpMethod,
    String path,
    RequestContext requestContext,
    Map<String, Object> headers,
    Map<String, Object> queryStringParameters,
    String body,
    boolean isBase64Encoded
) {
    public record RequestContext(Http http) {
        public record Http(String method, String path) {
        }
    }
}
