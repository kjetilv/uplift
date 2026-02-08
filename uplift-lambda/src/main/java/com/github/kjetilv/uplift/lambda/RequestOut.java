package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.util.Map;

 @JsonRecord
public record RequestOut(
    String version,
    String httpMethod,
    String path,
    Map<String, Object> headers,
    Map<String, Object> queryStringParameters,
    RequestContext requestContext,
    boolean isBase64Encoded,
    String body
) {

    public RequestOut(
        String httpMethod,
        String path,
        Map<String, Object> headers,
        Map<String, Object> queryStringParameters,
        String body
    ) {
        this(
            VERSION,
            httpMethod,
            path,
            headers,
            queryStringParameters,
            new RequestContext(
                new RequestContext.Http(httpMethod, path)
            ),
            false,
            body
        );
    }

    private static final String VERSION = "2.0";

    public record RequestContext(Http http) {

        public record Http(String method, String path) {
        }
    }
}
