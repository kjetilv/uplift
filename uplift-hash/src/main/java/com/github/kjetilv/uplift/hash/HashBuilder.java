package com.github.kjetilv.uplift.hash;

import module java.base;

/// Stateful interface for building ids.  Maintains an underlying hasher which can be [#hash(Object)] added to.
///
/// When done, [#build()] can be called to get the final hash, and reset the underlying hasher.
///
/// @param <T>
public interface HashBuilder<T, H extends HashKind<H>> {

    default void accept(T item) {
        hash(item);
    }

    default HashBuilder<T, H> hash(List<T> items) {
        for (var t : items) {
            accept(t);
        }
        return this;
    }

    default HashBuilder<T, H> hash(Stream<T> items) {
        items.forEach(this::accept);
        return this;
    }

    H kind();

    HashBuilder<T, H> hash(T item);

    /// Get the id, reset the underlying hasher.
    ///
    /// @return Hash
    Hash<H> build();

    /// @param transform Transformer for R to T
    /// @param <R>       Input type to new hasher
    /// @return New hasher that accepts and transforms its input to T
    <R> HashBuilder<R, H> map(Function<R, T> transform);

    <R> HashBuilder<R, H> flatMap(Function<R, Stream<T>> transform);
}
