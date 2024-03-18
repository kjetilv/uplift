package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.util.Map;

/**
 * Record structure that can capture both 1.0 and 2.0 inputs.
 */
@JsonRecord(root = true)
public record Request(
    String version,
    String httpMethod,
    String path,
    String rawPath,
    RequestContext requestContext,
    Map<String, Object> headers,
    Map<String, Object> queryStringParameters,
    String body
) {
    @JsonRecord
    public record RequestContext(Http http) {
        @JsonRecord
        public record Http(String method, String path) {
        }
    }
}
