package com.github.kjetilv.uplift.synchttp;

import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"WeakerAccess", "unused"})
public record CorsSettings(
    List<String> origins,
    List<String> methods,
    List<String> headers,
    Integer maxAge
) {

    public static final List<String> ALL_HOSTS = List.of("*");

    public static final int DEFAULT_MAX_AGE = 86400;

    public static final List<String> ONLY_GET = List.of("GET");

    public static final List<String> NO_HEADERS = List.of();

    public static final Map<String, Object> NO_ACCESS = Map.of();

    public CorsSettings(
        List<String> origins,
        List<String> methods
    ) {
        this(origins, methods, null, null);
    }

    public CorsSettings(
        List<String> origins,
        List<String> methods,
        List<String> headers
    ) {
        this(origins, methods, headers, null);
    }

    public CorsSettings(
        List<String> origins,
        List<String> methods,
        List<String> headers,
        Integer maxAge
    ) {
        this.origins = origins == null || origins.isEmpty() ? ALL_HOSTS : List.copyOf(origins);
        this.methods = methods == null || methods.isEmpty() ? ONLY_GET : List.copyOf(methods);
        this.headers = headers == null || headers.isEmpty() ? NO_HEADERS : List.copyOf(headers);
        this.maxAge = maxAge != null && maxAge > 0 ? maxAge : DEFAULT_MAX_AGE;
    }

    public HttpResponseCallback.Headers applyTo(String host, HttpResponseCallback.Headers headers) {
        return headers.headers(headerMap(host));
    }

    public Map<String, Object> headerMap(String host) {
        return originValue(host).
            <Map<String, Object>>map(origin -> Map.of(
            "access-control-allow-origin", origin,
            "access-control-allow-methods", methodsValue(),
            "access-control-allow-headers", headersValue(),
            "access-control-max-age", maxAge.toString(),
            "access-control-allow-credentials", credentialsValue()
        )).orElse(NO_ACCESS);
    }

    public boolean accepts(String origin) {
        return this.origins.stream().anyMatch(origin::equals);
    }

    public String toHeaderSection() {
        return null;
    }

    String headersValue() {
        return value(headers());
    }

    String methodsValue() {
        return value(methods());
    }

    String credentialsValue() {
        return String.valueOf(credentials());
    }

    Optional<String> originValue(String host) {
        return isStar()
            ? Optional.of("*")
            : origins.stream()
              .filter(host::equals)
              .findFirst();
    }

    boolean credentials() {
        return !isStar();
    }

    private boolean isStar() {
        return origins.equals(ALL_HOSTS);
    }

    private static String value(List<String> list) {
        return String.join(",", list);
    }
}
