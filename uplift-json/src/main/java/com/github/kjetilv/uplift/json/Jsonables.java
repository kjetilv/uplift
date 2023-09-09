package com.github.kjetilv.uplift.json;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Jsonables {

    public static void add(Class<?> type, Jsonable<?> jsonable) {
        Jsonable<?> existing = JSONABLES.putIfAbsent(type, jsonable);
        if (existing != null) {
            throw new IllegalArgumentException("Already present for " + type + ": " + existing);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Jsonable<T> get(Class<T> type) {
        return (Jsonable<T>) JSONABLES.get(type);
    }

    private Jsonables() {

    }

    private static final ConcurrentMap<Class<?>, Jsonable<?>> JSONABLES = new ConcurrentHashMap<>();
}
