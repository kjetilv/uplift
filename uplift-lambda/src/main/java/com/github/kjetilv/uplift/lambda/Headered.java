package com.github.kjetilv.uplift.lambda;

import module java.base;

interface Headered {

    default Optional<String> header(String name) {
        return maybeHeaders()
            .map(map -> map.get(name))
            .or(() ->
                maybeHeaders().map(map ->
                    caseInsensitive(map, name)))
            .map(String::valueOf);
    }

    default Optional<Map<String, Object>> maybeHeaders() {
        return Optional.ofNullable(headers());
    }

    Map<String, Object> headers();

    private static Optional<Object> caseInsensitive(Map<String, Object> headers, String name) {
        return headers.entrySet()
            .stream()
            .filter(entry ->
                entry.getKey().equalsIgnoreCase(name))
            .findFirst()
            .map(Map.Entry::getValue);
    }
}
