package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.kernel.io.BytesIO;

import java.util.*;

import static com.github.kjetilv.uplift.lambda.Utils.printBody;
import static java.util.Objects.requireNonNull;

public record LambdaPayload(
    String method,

    String path,

    Map<?, ?> queryParameters,

    Map<?, ?> headers,

    String body
) {

    public static LambdaPayload create(Map<?, ?> req) {
        if (req.isEmpty()) {
            throw new IllegalArgumentException("Empty request");
        }
        try {
            String version = stringVal(req, "version", "1.0");
            Map<?, ?> queryParameters = submap(req, "queryStringParameters");
            Map<?, ?> headers = submap(req, "headers");
            String body = body(req);
            switch (version) {
                case "2.0" -> {
                    Map<?, ?> http = submap(req, "requestContext", "http");
                    return new LambdaPayload(
                        stringVal(http, "method").toUpperCase(Locale.ROOT),
                        stringVal(http, "path"),
                        queryParameters,
                        headers,
                        body
                    );
                }
                case "1.0" -> {
                    return new LambdaPayload(
                        stringVal(req, "httpMethod").toUpperCase(Locale.ROOT),
                        stringVal(req, "path"),
                        queryParameters,
                        headers,
                        body
                    );
                }
                default -> throw new IllegalArgumentException("Unknown version: " + req);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Failed to parse payload: " + Json.INSTANCE.write(req), e);
        }
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
        return method.equalsIgnoreCase(this.method) && path.equalsIgnoreCase(this.path);
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

    @SuppressWarnings("StringEquality")
    private static String body(Map<?, ?> req) {
        String bodyString = stringVal(req, "body", BLANK);
        if (bodyString == BLANK || bodyString.isBlank()) {
            return null;
        }
        if (looksJsony(bodyString)) {
            return bodyString;
        }
        return base64(bodyString);
    }

    private static String base64(String bodyString) {
        return BytesIO.stringFromBase64(bodyString);
    }

    private static boolean looksJsony(String bodyString) {
        boolean lookJsony =
            wrapped('{', bodyString, '}') || wrapped('[', bodyString, ']');
        return lookJsony;
    }

    private static boolean wrapped(char start, CharSequence bodyString, char end) {
        return bodyString.charAt(0) == start || bodyString.charAt(bodyString.length() - 1) == end;
    }

    private static Map<?, ?> submap(Map<?, ?> root, String... keys) {
        Map<?, ?> map = root;
        for (String key : keys) {
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
}
