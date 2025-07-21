package com.github.kjetilv.uplift.hash;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Stateful interface for building ids.
 *
 * @param <T>
 */
public interface HashBuilder<T, H extends HashKind<H>>
    extends Consumer<T>, Function<T, HashBuilder<T, H>>, Supplier<Hash<H>> {

    H kind();

    @Override
    default void accept(T item) {
        hash(item);
    }

    @Override
    default HashBuilder<T, H> apply(T item) {
        return hash(item);
    }

    default HashBuilder<T, H> hash(List<T> items) {
        for (T t : items) {
            accept(t);
        }
        return this;
    }

    default HashBuilder<T, H> hash(Stream<T> items) {
        items.forEach(this);
        return this;
    }

    HashBuilder<T, H> hash(T item);

    /**
     * Get the id, reset the underlying hasher.
     *
     * @return Hash
     */
    @Override
    Hash<H> get();

    /**
     * @param transform Transformer for R to T
     * @param <R>       Input type to new hasher
     * @return New hasher that accepts and transforms its input to T
     */
    <R> HashBuilder<R, H> map(Function<R, T> transform);

    <R> HashBuilder<R, H> flatMap(Function<R, Stream<T>> transform);
}
