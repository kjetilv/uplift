package com.github.kjetilv.uplift.edamame;

import module java.base;

/// Behold the memoizer! Maps will be stored in canonical form, avoiding memory wasted on identical
/// trees.
///
/// In cases where the set of maps is known (and finite), the [complete][#complete()] method can
/// be invoked after all data are inserted. This allows further savings by throwing away internal
/// book-keeping state and locking down the memoizer for further puts.
///
/// Use the [MapsMemoizers] factory class to create instances.
///
/// Note: Extends [MemoizedMaps] to enable lookup of stored maps even before [completion][#complete()].
///
/// @param <I> Id type, used to identify maps
/// @param <K> Key type, used as keys in stored maps
/// @see MapsMemoizers
@SuppressWarnings("unused")
public interface MapsMemoizer<I, K> extends MemoizedMaps<I, K> {

    /// Store one map
    ///
    /// @param identifier Identifier
    /// @param value      Map
    /// @throws IllegalArgumentException If the identifier is already stored
    /// @throws IllegalStateException    If this instance is [completed][#complete()]
    void put(I identifier, Map<?, ?> value);

    /// Store one map, unless it's already stored
    ///
    /// @param identifier Identifier
    /// @param value      Map
    /// @return true iff the map was added.  If false, the memoizer was unchanged
    /// @throws IllegalStateException If this instance is [completed][#complete()]
    boolean putIfAbsent(I identifier, Map<?, ?> value);

    /// Signals the end of [putting][#put(Object,Map)] activities.  Locks down this instance
    /// for further calls to [put][#put(Object, Map)], allowing it to free up memory used for working data.
    ///
    /// @return This instance, typed to [MemoizedMaps] in order to discourage futher putting
    MemoizedMaps<I, K> complete();
}
