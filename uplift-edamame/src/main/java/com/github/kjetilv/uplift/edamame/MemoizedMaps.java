package com.github.kjetilv.uplift.edamame;

import module java.base;

/// Read-only access to memoized maps.
///
/// @param <I> Id type, used to identify maps
/// @param <MK> Key type, used as keys in stored maps
public interface MemoizedMaps<I, MK> {

    /// @return The number of maps memoized
    int size();

    /// @param id Id
    /// @return Stored map, or null of the identifier was unknown
    Map<MK, ?> get(I id);
}
