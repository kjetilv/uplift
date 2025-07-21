package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.KeyHandler;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

final class CanonicalKeysCatalog<K> implements KeyHandler<K> {

    private final Map<Object, K> canonicalKeys = new ConcurrentHashMap<>();

    private final Map<K, byte[]> canonicalBytes = new ConcurrentHashMap<>();

    private final KeyHandler<K> keyHandler;

    CanonicalKeysCatalog(KeyHandler<K> keyHandler) {
        this.keyHandler = Objects.requireNonNull(keyHandler, "delegate");
    }

    @Override
    public byte[] bytes(K key) {
        return canonicalBytes.computeIfAbsent(key, keyHandler::bytes);
    }

    @Override
    public K normalize(Object key) {
        return canonicalKeys.computeIfAbsent(key, keyHandler::normalize);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + canonicalKeys.size() + "]";
    }
}
