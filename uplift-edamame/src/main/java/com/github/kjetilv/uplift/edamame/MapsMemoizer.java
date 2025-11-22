package com.github.kjetilv.uplift.edamame;

import module java.base;

/// Maps memoizer. Use {@link MapsMemoizers} to build instances.
///
/// Stores maps will be stored in canonical forms, avoiding memory wasted on identical trees.
/// Extends {@link MemoizedMaps} and supports combined store/lookup behaviour.
///
/// Note that keeping track of known hashes and shared structures requires internal structures
/// that will also consume some memory. These are required as long as there is still data to be
/// inserted.
///
/// However, once all known data is in, it is possible to call the [maps][#maps()] method, producing a
/// dedicated lookup instance which holds only the canonical data. The original
/// {@link MapsMemoizer} may then be discarded, leaving these internal structures to be GC-ed.
///
/// @param <I> Top-level id type, used to lookup maps
/// @param <K> Key type, used as the key type in stored maps
/// @see MapsMemoizers
@SuppressWarnings("unused")
public interface MapsMemoizer<I, K> extends MemoizedMaps<I, K> {

    /// Returns a read-only access for canonical maps.  This memoizer should not be invoked with further
    /// calls to {@link #put(Object, Map) put}/{@link #putIfAbsent(Object, Map) putIfAbsent}, as it carries
    /// a risk of concurrent modifications.
    ///
    /// To get thread-safe copy, invoke {@link #maps(boolean) maps}
    /// with the copy flag.
    ///
    /// @return Lookup of canonical maps
    default MemoizedMaps<I, K> maps() {
        return maps(false);
    }

    /// Store one map
    ///
    /// @param identifier Identifier
    /// @param value      Map
    /// @throws IllegalArgumentException If the identifier is already stored
    void put(I identifier, Map<?, ?> value);

    /// Store one map, unless it's already stored
    ///
    /// @param identifier Identifier
    /// @param value      Map
    /// @return true iff the map was added.  If false, the memoizer was unchanged
    boolean putIfAbsent(I identifier, Map<?, ?> value);

    /// Returns a lookup interface for canonical maps
    ///
    /// @param copy If true, return a copy of the stored maps and leave this memoizer able to accept further
    ///             calls to {@link #put(Object, Map) put}/{@link #putIfAbsent(Object, Map) putIfAbsent}.
    /// @return Lookup of canonical maps
    MemoizedMaps<I, K> maps(boolean copy);
}
