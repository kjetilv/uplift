package com.github.kjetilv.uplift.hash;

import com.github.kjetilv.uplift.kernel.io.Bytes;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Stateful interface for building ids.
 *
 * @param <T>
 */
public interface HashBuilder<T, H extends HashKind<H>> {

    default void accept(T item) {
        hash(item);
    }

    default HashBuilder<T, H> hash(List<T> items) {
        for (T t : items) {
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

    /**
     * Get the id, reset the underlying hasher.
     *
     * @return Hash
     */
    Hash<H> build();

    /**
     * @param toBytes Function from input type R to a stream of bytes
     * @param <R>     Input type
     * @return A hasher which accepts R inputs and adds them to the hash
     */
    <R> HashBuilder<R, H> also(Function<R, Stream<Bytes>> toBytes);

    /**
     * @param transform Transformer for R to T
     * @param <R>       Input type to new hasher
     * @return New hasher that accepts and transforms its input to T
     */
    <R> HashBuilder<R, H> map(Function<R, T> transform);

    <R> HashBuilder<R, H> flatMap(Function<R, Stream<T>> transform);
}
