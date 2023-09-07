package com.github.kjetilv.uplift.flambda;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.kjetilv.uplift.asynchttp.HttpReq;
import com.github.kjetilv.uplift.kernel.io.BytesIO;

import static java.util.Objects.requireNonNull;

public record LambdaRequest(String id, HttpReq request) {

    public LambdaRequest(HttpReq httpReq) {
        this(null, httpReq);
    }

    public LambdaRequest(String id, HttpReq request) {
        this.id = id;
        this.request = requireNonNull(request, "request");
    }

    public LambdaRequest withId(String id) {
        return new LambdaRequest(requireNonNull(id, "id"), request);
    }

    Map<String, Object> toPayload() {
        return Map.of(
            "version", "2.0",
            "httpMethod", request.method(),
            "path", request.path(),
            "headers", toSingleValue(request.headers()),
            "queryStringParameters", toSingleValue(request.queryParams()),
            "requestContext", Map.of(
                "http", Map.of(
                    "method", request.method(),
                    "path", request.path()
                )
            ),
            "isBase64Encoded", true,
            "body", BytesIO.toBase64(request.body())
        );
    }

    JsonLambdaPayload toJsonPayload() {
        return new JsonLambdaPayload(
            "2.0",
            request.method(),
            request.path(),
            toSingleValue(request.headers()),
            toSingleValue(request.queryParams()),
            new JsonLambdaPayload.RequestContext(
                new JsonLambdaPayload.RequestContext.Http(
                    request.method(),
                    request.path()
                )),
            true,
            BytesIO.toBase64(request.body())
        );
    }

    private static Map<String, String> toSingleValue(Map<String, ? extends List<String>> map) {
        return map.entrySet().stream().collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    List<String> values = entry.getValue();
                    int size = values.size();
                    return size == 0 ? ""
                        : size == 1 ? String.valueOf(values.get(0))
                            : concated(values, size);
                }
            )
        );
    }

    private static String concated(List<String> values, int size) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean firstDone = false;
        for (int i = 0; i < size; i++) {
            if (firstDone) {
                stringBuilder.append(",");
            } else {
                firstDone = true;
            }
            stringBuilder.append(values.get(i));
        }
        return stringBuilder.toString();
    }

    record JsonLambdaPayload(
        String version,
        String httpMethod,
        String path,
        Map<String, String> headers,
        Map<String, String> queryStringParameters,
        RequestContext requestContext,
        boolean isBase64Encoded,
        String body
    ) {

        @SuppressWarnings("WeakerAccess")
        record RequestContext(
            Http http
        ) {

            record Http(
                String method,
                String path
            ) {

            }
        }
    }
}
