package com.github.kjetilv.uplift.edamame.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("NullableProblems")
public record CanKey(String key) implements Comparable<CanKey> {

    @Override
    public int compareTo(CanKey o) {
        return key.compareTo(o.key);
    }

    private static final Map<String, CanKey> canon = new ConcurrentHashMap<>();

    public static CanKey get(String key) {
        return canon.computeIfAbsent(key, CanKey::new);
    }

    @Override
    public String toString() {
        return key;
    }
}
