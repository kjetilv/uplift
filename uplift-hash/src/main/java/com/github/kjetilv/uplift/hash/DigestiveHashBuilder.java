package com.github.kjetilv.uplift.hash;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

record DigestiveHashBuilder<T, H extends HashKind<H>>(
    ByteDigest<H> byteDigest,
    Function<T, Stream<Bytes>> toBytes
)
    implements HashBuilder<T, H> {

    DigestiveHashBuilder(ByteDigest<H> byteDigest, Function<T, Stream<Bytes>> toBytes) {
        this.byteDigest = Objects.requireNonNull(byteDigest, "byteDigest");
        this.toBytes = Objects.requireNonNull(toBytes, "toBytes");
    }

    @Override
    public H kind() {
        return byteDigest.kind();
    }

    @Override
    public HashBuilder<T, H> hash(T item) {
        Stream.ofNullable(item)
            .flatMap(toBytes)
            .forEach(byteDigest::digest);
        return this;
    }

    @Override
    public Hash<H> get() {
        return byteDigest.get();
    }

    public <R> HashBuilder<R, H> also(Function<R, Stream<Bytes>> toBytes) {
        return new DigestiveHashBuilder<>(byteDigest, toBytes);
    }

    @Override
    public <R> HashBuilder<R, H> map(Function<R, T> transform) {
        return new DigestiveHashBuilder<>(byteDigest, transform.andThen(toBytes));
    }

    @Override
    public <R> HashBuilder<R, H> flatMap(Function<R, Stream<T>> transform) {
        return new DigestiveHashBuilder<>(byteDigest, r -> transform.apply(r).flatMap(toBytes));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + byteDigest + " <- " + toBytes + "]";
    }
}
