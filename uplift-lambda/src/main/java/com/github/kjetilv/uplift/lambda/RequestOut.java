package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.kernel.io.Range;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/// A Lambda event from API Gateway. Covers payload format 1.0 (`httpMethod`, `path`) and 2.0 (`routeKey`,
/// `rawPath`, `rawQueryString`, `cookies`, `requestContext.http`). Fields of the other format are null.
@JsonRecord
public record RequestOut(
    String version,
    String routeKey,
    String httpMethod,
    String path,
    String rawPath,
    String rawQueryString,
    List<String> cookies,
    Map<String, Object> headers,
    Map<String, Object> queryStringParameters,
    RequestContext requestContext,
    boolean isBase64Encoded,
    String body
) implements Headered {

    public RequestOut(
        String httpMethod,
        String path,
        Map<String, Object> headers,
        Map<String, Object> queryStringParameters,
        String body
    ) {
        this(
            VERSION,
            null,
            httpMethod,
            path,
            null,
            null,
            null,
            headers,
            queryStringParameters,
            new RequestContext(
                new RequestContext.Http(httpMethod, path)
            ),
            false,
            body
        );
    }

    Optional<Range> range() {
        return header("Range").flatMap(Range::read);
    }

    private static final String VERSION = "2.0";

    /// Request context. Format 1.0 events have more fields, which are not read.
    public record RequestContext(
        String accountId,
        String apiId,
        String domainName,
        String domainPrefix,
        Http http,
        String requestId,
        String routeKey,
        String stage,
        String time,
        Long timeEpoch
    ) {

        public RequestContext(Http http) {
            this(null, null, null, null, http, null, null, null, null, null);
        }

        public record Http(String method, String path, String protocol, String sourceIp, String userAgent) {

            public Http(String method, String path) {
                this(method, path, null, null, null);
            }
        }
    }

    @Override
    public String toString() {
        var method = httpMethod != null ? httpMethod
            : requestContext != null && requestContext.http() != null ? requestContext.http().method()
                : null;
        return getClass().getSimpleName() + "[" + method + " " + (path != null ? path : rawPath) +
               range().map(Range::rangeRequest)
                   .map(range -> "/" + range).orElse("") +
               " q:" + Utils.printQueryParams(queryStringParameters) +
               " h:" + Utils.headers(headers) +
               " b:" + Utils.printBody(body, isBase64Encoded ? "base64" : null) +
               "]";
    }
}
