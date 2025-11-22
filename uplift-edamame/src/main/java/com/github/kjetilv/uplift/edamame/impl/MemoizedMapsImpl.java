package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.MemoizedMaps;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Map;

final class MemoizedMapsImpl<I, K, H extends HashKind<H>> implements MemoizedMaps<I, K> {

    private final Map<I, Hash<H>> hashes;

    private final Map<Hash<H>, Map<K, Object>> objects;

    private final Map<I, Map<K, Object>> overflow;

    private final int size;

    MemoizedMapsImpl(
        Map<I, Hash<H>> hashes,
        Map<Hash<H>, Map<K, Object>> objects,
        Map<I, Map<K, Object>> overflow
    ) {
        this.hashes = hashes;
        this.objects = objects;
        this.overflow = overflow;
        this.size = hashes.size() + overflow.size();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Map<K, ?> get(I id) {
        var hash = hashes.get(id);
        return hash != null ? objects.get(hash)
            : overflow.get(id);
    }
}
