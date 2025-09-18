package com.github.kjetilv.uplift.edam.internal;

/// Indexing interface.  [Indexes][#exchange(Object)] each object to a unique long, and allows
/// later [retrieval][#exchange(long)] of the object.
///
/// @param <T>
interface Indexer<T> {

    /// @param t Object to map
    /// @return Unique long for the given object
    /// @throws IllegalStateException If the limit is reached
    long exchange(T t);

    /// Retrieve the object mapped to the given index.
    ///
    /// @param index Unique long, previously returned by [mapping][#exchange(Object)]
    /// @return Hashed object
    /// @throws IllegalArgumentException If the hash could not be found
    T exchange(long index);

    /// The number of objects that can be mapped, at the most.
    ///
    /// @return limit
    long limit();
}
