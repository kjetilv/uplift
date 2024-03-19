package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.asynchttp.HttpReq;
import com.github.kjetilv.uplift.kernel.io.BytesIO;
import com.github.kjetilv.uplift.kernel.util.Maps;
import com.github.kjetilv.uplift.lambda.RequestOut;

import java.util.List;
import java.util.Map;

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

    RequestOut out() {
        return new RequestOut(
            request.method(),
            request.path(),
            toSingleValue(request.headers()),
            toSingleValue(request.queryParams()),
            BytesIO.toBase64(request.body())
        );
    }

    private static Map<String, Object> toSingleValue(Map<String, ? extends List<String>> map) {
        return map == null || map.isEmpty()
            ? null
            : Maps.mapValues(map, values ->
                switch (values.size()) {
                    case 0 -> "";
                    case 1 -> String.valueOf(values.getFirst());
                    default -> cat(values, values.size());
                });
    }

    private static String cat(List<String> values, int size) {
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
}
