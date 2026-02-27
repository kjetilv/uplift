package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.util.Bytes;

final class CanonicalKeysCataloguer<MK> implements KeyHandler<MK> {

    private final Map<Object, MK> canonicalKeys = new ConcurrentHashMap<>();

    private final Map<MK, Bytes> canonicalBytes = new ConcurrentHashMap<>();

    private final KeyHandler<MK> keyHandler;

    CanonicalKeysCataloguer(KeyHandler<MK> keyHandler) {
        this.keyHandler = Objects.requireNonNull(keyHandler, "delegate");
    }

    @Override
    public Bytes bytes(MK key) {
        return canonicalBytes.computeIfAbsent(key, keyHandler::bytes);
    }

    @Override
    public MK normalize(Object key) {
        return canonicalKeys.computeIfAbsent(key, keyHandler::normalize);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + canonicalKeys.size() + "]";
    }
}
