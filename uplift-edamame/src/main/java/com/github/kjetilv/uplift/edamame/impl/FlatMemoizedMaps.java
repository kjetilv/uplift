package com.github.kjetilv.uplift.edamame.impl;

import module java.base;
import com.github.kjetilv.uplift.edamame.MemoizedMaps;

/// Immutable lookup with one map from identifier to stored map
///
/// @param maps Stored maps by identifier
record FlatMemoizedMaps<I, K>(Map<I, Map<K, ?>> maps) implements MemoizedMaps<I, K> {

    @Override
    public int size() {
        return maps.size();
    }

    @Override
    public Map<K, ?> get(I id) {
        return maps.get(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + maps.size() + "]";
    }
}
