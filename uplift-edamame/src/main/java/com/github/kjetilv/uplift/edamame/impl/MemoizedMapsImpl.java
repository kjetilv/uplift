package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.MemoizedMaps;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Map;

class MemoizedMapsImpl<I, K, H extends HashKind<H>> implements MemoizedMaps<I, K> {

    private final Map<I, Hash<H>> memoizedHashes;

    private final Map<Hash<H>, Map<K, Object>> canonicalObjects;

    private final Map<I, Map<K, Object>> overflowObjects;

    private final int size;

    public MemoizedMapsImpl(
        Map<I, Hash<H>> memoizedHashes,
        Map<Hash<H>, Map<K, Object>> canonicalObjects,
        Map<I, Map<K, Object>> overflowObjects
    ) {
        this.memoizedHashes = memoizedHashes;
        this.canonicalObjects = canonicalObjects;
        this.overflowObjects = overflowObjects;
        this.size = memoizedHashes.size() + overflowObjects.size();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Map<K, ?> get(I identifier) {
        var hash = memoizedHashes.get(identifier);
        return hash == null
            ? overflowObjects.get(identifier)
            : canonicalObjects.get(hash);
    }
}
