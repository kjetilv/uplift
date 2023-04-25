package com.github.kjetilv.uplift.lambda;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.kernel.io.BytesIO;

import static com.github.kjetilv.uplift.lambda.Utils.printBody;
import static java.util.Objects.requireNonNull;

public final class LambdaPayload {

    public static LambdaPayload create(Map<?, ?> req) {
        return new LambdaPayload(req);
    }

    private final String path;

    private final Map<?, ?> headers;

    private final String method;

    private final String body;

    private final Map<?, ?> queryParameters;

    private LambdaPayload(Map<?, ?> req) {
        if (req.isEmpty()) {
            throw new IllegalArgumentException("Empty request");
        }
        try {
            String version = stringVal(req, "version", "1.0");
            switch (version) {
                case "2.0" -> {
                    Map<?, ?> http = submap(req, "requestContext", "http");
                    this.method = stringVal(http, "method").toUpperCase(Locale.ROOT);
                    this.path = stringVal(http, "path");
                }
                case "1.0" -> {
                    this.method = stringVal(req, "httpMethod").toUpperCase(Locale.ROOT);
                    this.path = stringVal(req, "path");
                }
                default -> throw new IllegalArgumentException("Unknown version: " + req);
            }
            this.headers = submap(req, "headers");
            this.queryParameters = submap(req, "queryStringParameters");
            this.body = body(req);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Failed to parse payload: " + Json.INSTANCE.write(req), e);
        }
    }

    public String method() {
        return method;
    }

    public boolean isPost() {
        return isMethod("POST");
    }

    public String path() {
        return path(null);
    }

    public String path(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return path;
        }
        if (path.equals(prefix)) {
            return "";
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
        return method.equalsIgnoreCase(this.method) && path.equalsIgnoreCase(this.path);
    }

    public Optional<String> pathParam(String pattern, String name) {
        return Params.param(path, pattern, name);
    }

    public String queryParam(String key) {
        return stringVal(queryParameters, key, null);
    }

    public Optional<String> header(String header) {
        return headers.entrySet().stream()
            .filter(entry ->
                String.valueOf(entry.getKey()).equalsIgnoreCase(header))
            .map(Map.Entry::getValue)
            .map(String::valueOf)
            .findFirst();
    }

    public String body() {
        return body;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean isMethod(String method) {
        return requireNonNull(method, "method").equalsIgnoreCase(this.method);
    }

    private static String body(Map<?, ?> req) {
        String bodyString = stringVal(req, "body", "");
        if (bodyString.isBlank()) {
            return null;
        }
        boolean json =
            wrapped('{', bodyString, '}') || wrapped('[', bodyString, ']');
        return bodyString.isBlank() ? null
            : json ? bodyString
                : BytesIO.stringFromBase64(bodyString);
    }

    private static boolean wrapped(char start, CharSequence bodyString, char end) {
        return bodyString.charAt(0) == start || bodyString.charAt(bodyString.length() - 1) == end;
    }

    private static Map<?, ?> submap(Map<?, ?> root, String... keys) {
        Map<?, ?> map = root;
        for (String key: keys) {
            Object value = map.get(key);
            if (value == null) {
                return Collections.emptyMap();
            }
            if (value instanceof Map<?, ?> level) {
                map = level;
            } else {
                throw new IllegalStateException("Not a level in " + root + ": " + Arrays.toString(keys));
            }
        }
        return map == null ? Collections.emptyMap() : map;
    }

    private static <V> String stringVal(Map<?, ? super V> map, String key) {
        return stringVal(map, key, null);
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + method + " " + path +
               (queryParameters == null || queryParameters.isEmpty() ? "" : "﹖" + queryParameters) +
               (body == null || body.isBlank() ? "" : " ⨁ " + printBody(body)) +
               "]]";
    }
}
