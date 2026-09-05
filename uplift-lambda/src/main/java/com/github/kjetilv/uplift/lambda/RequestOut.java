package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.anno.JsonRecord;
import com.github.kjetilv.uplift.kernel.io.Range;

import java.util.Map;
import java.util.Optional;

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

    Optional<Range> range() {
        return header("Range").flatMap(Range::read);
    }

    private static final String VERSION = "2.0";

    public record RequestContext(Http http) {

        public record Http(String method, String path) {
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + httpMethod + " " + path +
               range().map(Range::rangeRequest)
                   .map(range -> "/" + range).orElse("") +
               " q:" + Utils.printQueryParams(queryStringParameters) +
               " h:" + Utils.headers(headers) +
               " b:" + Utils.printBody(body, isBase64Encoded ? "base64" : null) +
               "]";
    }
}
