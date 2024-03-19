package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.util.Map;

@JsonRecord(root = true)
public record RequestOut(
    String version,
    String httpMethod,
    String path,
    Map<String, Object> headers,
    Map<String, Object> queryStringParameters,
    RequestContextOut requestContext,
    boolean isBase64Encoded,
    String body
) {

    public RequestOut {
        if (version != null && !version.equals(VERSION)) {
            throw new IllegalStateException("Version can only be set to " + VERSION + ": " + version);
        }
    }

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
            new RequestContextOut(
                new RequestContextOut.HttpOut(httpMethod, path)
            ),
            true,
            body
        );
    }

    private static final String VERSION = "2.0";

    public record RequestContextOut(HttpOut http) {

        public record HttpOut(String method, String path) {

        }
    }
}
