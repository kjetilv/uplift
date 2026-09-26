package com.github.kjetilv.uplift.edamame.test;

import module java.base;

public record CanKey(String key) implements Comparable<CanKey> {

    public static CanKey get(String key) {
        return canon.computeIfAbsent(key, CanKey::new);
    }

    @Override
    public int compareTo(CanKey o) {
        return key.compareTo(o.key);
    }

    private static final Map<String, CanKey> canon = new ConcurrentHashMap<>();

    @Override
    public String toString() {
        return key;
    }
}
