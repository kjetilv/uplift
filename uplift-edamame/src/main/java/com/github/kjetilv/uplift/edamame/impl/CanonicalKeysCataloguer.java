package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.util.Bytes;

final class CanonicalKeysCataloguer<K> implements KeyHandler<K> {

    private final Map<Object, K> canonicalKeys = new ConcurrentHashMap<>();

    private final Map<K, Bytes> canonicalBytes = new ConcurrentHashMap<>();

    private final KeyHandler<K> keyHandler;

    CanonicalKeysCataloguer(KeyHandler<K> keyHandler) {
        this.keyHandler = Objects.requireNonNull(keyHandler, "delegate");
    }

    @Override
    public Bytes bytes(K key) {
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
