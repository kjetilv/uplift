package com.github.kjetilv.uplift.hash;

import module java.base;
import com.github.kjetilv.uplift.util.Bytes;

record DigestiveHashBuilder<T, H extends HashKind<H>>(
    ByteDigest<H> byteDigest,
    Function<T, Stream<Bytes>> toBytes
) implements HashBuilder<T, H> {

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
        return item == null ? this : hash(Stream.ofNullable(item));
    }

    @Override
    public HashBuilder<T, H> hash(List<T> items) {
        return items == null || items.isEmpty() ? this : hash(items.stream());
    }

    @Override
    public HashBuilder<T, H> hash(Stream<T> items) {
        if (items != null) {
            items.flatMap(toBytes)
                .forEach(byteDigest::digest);
        }
        return this;
    }

    @Override
    public Hash<H> build() {
        return byteDigest.get();
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
