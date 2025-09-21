package com.github.kjetilv.uplift.lambda;

import module java.base;
import module uplift.kernel;
import module uplift.util;

import static com.github.kjetilv.uplift.lambda.Utils.printBody;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public record LambdaPayload(
    String method,
    String path,
    Map<String, String> queryParameters,
    Map<String, String> headers,
    String body,
    Map<?, ?> source
) {

    public static LambdaPayload parse(String json) {
        return payload(json, RequestInRW.INSTANCE.stringReader().read(json));
    }

    public static LambdaPayload parse(InputStream json) {
        return payload(json, RequestInRW.INSTANCE.streamReader().read(json));
    }

    private static LambdaPayload payload(Object json, RequestIn req) {
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
        return stringVal(queryParameters, key);
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

    private static LambdaPayload lambda10(RequestIn req) {
        return new LambdaPayload(
            req.httpMethod(),
            req.path(),
            Maps.mapValues(req.queryStringParameters(), String::valueOf),
            Maps.mapValues(req.headers(), String::valueOf),
            reqBody(req),
            null
        );
    }

    private static LambdaPayload lambda20(RequestIn req) {
        return new LambdaPayload(
            req.requestContext().http().method(),
            req.requestContext().http().path(),
            Maps.mapValues(req.queryStringParameters(), String::valueOf),
            Maps.mapValues(req.headers(), String::valueOf),
            reqBody(req),
            null
        );
    }

    private static String reqBody(RequestIn req) {
        if (req.body() == null || req.body().isEmpty()) {
            return null;
        }
        return req.isBase64Encoded() ? base64(req.body()) : req.body();
    }

    private static <V> String stringVal(Map<?, ? super V> map, String key) {
        Object value = map.get(key);
        if (value != null) {
            return value.toString().trim();
        }
        throw new IllegalArgumentException(
            "No string field `" + key + "`" + (map.isEmpty() ? ", no query params" : " in query params: " + map));
    }

    private static String base64(String bodyString) {
        return BytesIO.stringFromBase64(bodyString);
    }
}
