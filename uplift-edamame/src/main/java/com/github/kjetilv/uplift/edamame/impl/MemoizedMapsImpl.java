package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.MemoizedMaps;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Map;

import static java.util.Objects.requireNonNull;

final class MemoizedMapsImpl<I, MK, K extends HashKind<K>> implements MemoizedMaps<I, MK> {

    private final Map<I, Hash<K>> hashes;

    private final Map<Hash<K>, Map<MK, Object>> objects;

    private final Map<I, Map<MK, Object>> overflow;

    private final int size;

    MemoizedMapsImpl(
        Map<I, Hash<K>> hashes,
        Map<Hash<K>, Map<MK, Object>> objects,
        Map<I, Map<MK, Object>> overflow
    ) {
        this.hashes = requireNonNull(hashes, "hashes");
        this.objects = requireNonNull(objects, "objects");
        this.overflow = requireNonNull(overflow, "overflow");
        this.size = hashes.size() + overflow.size();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Map<MK, ?> get(I id) {
        var hash = hashes.get(id);
        return hash != null ? objects.get(hash)
            : overflow.get(id);
    }
}
