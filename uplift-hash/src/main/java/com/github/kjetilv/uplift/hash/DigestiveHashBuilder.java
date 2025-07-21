package com.github.kjetilv.uplift.hash;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

final class DigestiveHashBuilder<T, K extends HashKind<K>>
    implements HashBuilder<T, K> {

    private final ByteDigest<K> byteDigest;

    private final Function<T, Stream<Bytes>> toBytes;

    DigestiveHashBuilder(ByteDigest<K> byteDigest, Function<T, Stream<Bytes>> toBytes) {
        this.byteDigest = Objects.requireNonNull(byteDigest, "byteDigest");
        this.toBytes = Objects.requireNonNull(toBytes, "toBytes");
    }

    @Override
    public K kind() {
        return byteDigest.kind();
    }

    @Override
    public HashBuilder<T, K> hash(T item) {
        Stream.ofNullable(item)
            .flatMap(toBytes)
            .forEach(byteDigest::digest);
        return this;
    }

    @Override
    public Hash<K> get() {
        return byteDigest.get();
    }

    @Override
    public <R> HashBuilder<R, K> map(Function<R, T> transform) {
        return new DigestiveHashBuilder<>(byteDigest, transform.andThen(toBytes));
    }

    @Override
    public <R> HashBuilder<R, K> flatMap(Function<R, Stream<T>> transform) {
        return new DigestiveHashBuilder<>(byteDigest, r -> transform.apply(r).flatMap(toBytes));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + byteDigest + " <- " + toBytes + "]";
    }
}
