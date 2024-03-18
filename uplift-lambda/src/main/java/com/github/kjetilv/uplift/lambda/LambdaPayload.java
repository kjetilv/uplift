package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.kernel.util.Maps;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.github.kjetilv.uplift.lambda.Utils.printBody;
import static java.util.Objects.requireNonNull;

public record LambdaPayload(
    String method,
    String path,
    Map<String, String> queryParameters,
    Map<String, String> headers,
    String body,
    Map<?, ?> source
) {

    public static LambdaPayload parse(String json) {
        return payload(json, RequestRW.INSTANCE.stringReader().read(json));
    }

    public static LambdaPayload parse(InputStream json) {
        return payload(json, RequestRW.INSTANCE.streamReader().read(json));
    }

    private static LambdaPayload payload(Object json, Request req) {
        String version = req.version();
        if (version == null) {
            return lambda10(req);
        }
        return switch (version) {
            case "1.0" -> lambda10(req);
            case "2.0" -> lambda20(req);
            default -> throw new IllegalArgumentException("Unknown version: " + json);
        };
    }

    public boolean isPost() {
        return isMethod("POST");
    }

    public String path(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return path;
        }
        if (path.equals(prefix)) {
            return BLANK;
        }
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }
        throw new IllegalArgumentException(this + " is not prefixed with " + prefix);
    }

    public boolean isPrefixed(String method, String path) {
        return this.method.equalsIgnoreCase(method) && this.path.startsWith(path.toLowerCase(Locale.ROOT));
    }

    public boolean isExactly(String method, String path) {
        return method.equalsIgnoreCase(this.method) && isExactly(path);
    }

    public boolean isExactly(String path) {
        return path.equalsIgnoreCase(this.path);
    }

    public Optional<String> pathParam(String pattern, String name) {
        return Params.param(path, pattern, name);
    }

    public String queryParam(String key) {
        return stringVal(queryParameters, key, null);
    }

    public Optional<String> header(String header) {
        return headers.entrySet()
            .stream()
            .filter(entry ->
                String.valueOf(entry.getKey()).equalsIgnoreCase(header))
            .map(Map.Entry::getValue)
            .map(String::valueOf)
            .findFirst();
    }

    public String body() {
        return body;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + method + " " + path +
               (queryParameters == null || queryParameters.isEmpty() ? BLANK : "﹖" + queryParameters) +
               (body == null || body.isBlank() ? BLANK : " ⨁ " + printBody(body)) +
               "]]";
    }

    @SuppressWarnings("SameParameterValue")
    private boolean isMethod(String method) {
        return requireNonNull(method, "method").equalsIgnoreCase(this.method);
    }

    private static final String BLANK = "";

    private static LambdaPayload lambda10(Request req) {
        return new LambdaPayload(
            req.httpMethod(),
            req.path(),
            Maps.mapValues(req.queryStringParameters(), String::valueOf),
            Maps.mapValues(req.headers(), String::valueOf),
            req.body(),
            null
        );
    }

    private static LambdaPayload lambda20(Request req) {
        return new LambdaPayload(
            req.requestContext().http().method(),
            req.requestContext().http().path(),
            Maps.mapValues(req.queryStringParameters(), String::valueOf),
            Maps.mapValues(req.headers(), String::valueOf),
            req.body(),
            null
        );
    }

    private static <V> String stringVal(Map<?, ? super V> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value != null) {
            return value.toString().trim();
        }
        if (defaultValue != null) {
            return defaultValue;
        }
        throw new IllegalArgumentException(
            "No string field `" + key + "`" + (map.isEmpty() ? ", no query params" : " in query params: " + map));
    }
}
