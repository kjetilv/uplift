package com.github.kjetilv.uplift.hash;

import module java.base;

/// Stateful interface for building hashes.  Maintains an underlying hasher which can be progressively
/// [#hash(Object)] added to.
///
/// When done, [#build()] returns the final hash, and resets the underlying hasher.
///
/// @param <T> Hashed type
/// @param <H> Hash kind
public interface HashBuilder<T, H extends HashKind<H>> {

    /// Hash items
    /// @param items Items
    /// @return This builder
    default HashBuilder<T, H> hash(List<T> items) {
        items.forEach(this::hash);
        return this;
    }

    /// Hash items
    /// @param items Items
    /// @return This builder
    default HashBuilder<T, H> hash(Stream<T> items) {
        items.forEach(this::hash);
        return this;
    }

    /// @return Hash kind
    H kind();

    /// Add to the hash
    ///
    HashBuilder<T, H> hash(T item);

    /// Get the hash, reset the underlying hasher.
    ///
    /// @return Hash
    Hash<H> build();

    /// @param transform Transformer for R to T
    /// @param <R>       Input type to new hasher
    /// @return New hasher that accepts and transforms its input to T
    <R> HashBuilder<R, H> map(Function<R, T> transform);

    <R> HashBuilder<R, H> flatMap(Function<R, Stream<T>> transform);
}
