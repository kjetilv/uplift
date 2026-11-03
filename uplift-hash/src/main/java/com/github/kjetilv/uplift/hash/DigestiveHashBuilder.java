package com.github.kjetilv.uplift.hash;

import module java.base;
import com.github.kjetilv.uplift.util.Bytes;

/// Hash builder over a [ByteDigest]. Not thread-safe: use one builder per thread at a time.
///
/// @param byteDigest Digest
/// @param toBytes    Converts an item to bytes, possibly several chunks
/// @param single     Converts an item to one chunk of bytes, or null if the conversion may give several. When
///                   present, items are hashed without creating a stream per item.
record DigestiveHashBuilder<T, H extends HashKind<H>>(
    ByteDigest<H> byteDigest,
    Function<T, Stream<Bytes>> toBytes,
    Function<T, Bytes> single
) implements HashBuilder<T, H> {

    DigestiveHashBuilder(ByteDigest<H> byteDigest, Function<T, Stream<Bytes>> toBytes) {
        this(byteDigest, toBytes, null);
    }

    DigestiveHashBuilder {
        Objects.requireNonNull(byteDigest, "byteDigest");
        Objects.requireNonNull(toBytes, "toBytes");
    }

    /// @return Builder for bytes, hashing each item directly
    static <H extends HashKind<H>> DigestiveHashBuilder<Bytes, H> forBytes(ByteDigest<H> byteDigest) {
        return new DigestiveHashBuilder<>(byteDigest, IDENTITY, Function.identity());
    }

    @Override
    public H kind() {
        return byteDigest.kind();
    }

    @Override
    public HashBuilder<T, H> hash(T item) {
        if (item == null) {
            return this;
        }
        if (single != null) {
            byteDigest.digest(single.apply(item));
            return this;
        }
        toBytes.apply(item).forEach(byteDigest::digest);
        return this;
    }

    @Override
    public HashBuilder<T, H> hash(List<T> items) {
        if (items != null) {
            items.forEach(this::hash);
        }
        return this;
    }

    @Override
    public HashBuilder<T, H> hash(Stream<T> items) {
        if (items != null) {
            items.forEach(this::hash);
        }
        return this;
    }

    @Override
    public Hash<H> build() {
        return byteDigest.get();
    }

    @Override
    public <R> HashBuilder<R, H> map(Function<R, T> transform) {
        return new DigestiveHashBuilder<>(
            byteDigest,
            transform.andThen(toBytes),
            single == null ? null : transform.andThen(single)
        );
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
