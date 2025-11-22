package com.github.kjetilv.uplift.edamame;

import module java.base;

/// Provides access to memoized maps after [completion][#complete()].
///
/// @param <I> Id type, used to identify maps
/// @param <K> Key type, used as keys in stored maps
public interface MemoizedMaps<I, K> {

    /// @return The number of maps memoized
    int size();

    /// @param id Id
    /// @return Stored map, or null of the identifier was unknown
    Map<K, ?> get(I id);
}
