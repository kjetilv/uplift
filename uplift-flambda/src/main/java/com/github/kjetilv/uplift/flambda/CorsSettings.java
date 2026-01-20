package com.github.kjetilv.uplift.flambda;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public record CorsSettings(
    List<String> origins,
    List<String> methods,
    List<String> headers
) {

    public CorsSettings(List<String> origins, List<String> methods) {
        this(origins, methods, null);
    }

    public CorsSettings(List<String> origins, List<String> methods, List<String> headers) {
        this.origins = origins == null || origins.isEmpty() ? List.of("*") : List.copyOf(origins);
        this.methods = methods == null || methods.isEmpty() ? List.of("GET") : List.copyOf(methods);
        this.headers = headers == null || headers.isEmpty() ? List.of("content-type") : List.copyOf(headers);
    }

    public boolean accepts(String origin) {
        return this.origins.stream().anyMatch(origin::equals);
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

    String originValue() {
        return value(origins());
    }

    boolean credentials() {
        return origins.size() > 1 || !origins.getFirst().equals("*");
    }

    private static String value(List<String> list) {
        return String.join(",", list);
    }
}
