package com.github.kjetilv.uplift.lambda;

import java.util.Map;
import java.util.Optional;

interface Headered {

    default Optional<String> header(String name) {
        var headers = headers();
        return Optional.ofNullable(headers.get(name))
            .or(() -> caseInsensitive(headers, name))
            .map(String::valueOf);
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
