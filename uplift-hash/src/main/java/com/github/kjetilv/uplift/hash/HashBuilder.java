package com.github.kjetilv.uplift.hash;

import module java.base;
import com.github.kjetilv.uplift.util.Bytes;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;

/// Stateful interface for building hashes from input type `T`.
///
/// Maintains an underlying hasher which can be progressively added to:
/// * {@link #hash(Object)}
/// * {@link #hash(List)}
/// * {@link #hash(Stream)}
///
/// When done, invoking [#build()] returns the final hash, and resets the underlying hasher.
///
/// @param <T> Hashed type
/// @param <H> Hash kind
public interface HashBuilder<T, H extends HashKind<H>> {

    Function<Bytes, Stream<Bytes>> IDENTITY = Stream::of;

    static <H extends HashKind<H>> HashBuilder<byte[], H> forBytes(H kind) {
        return forKind(kind).map(Bytes::from);
    }

    static <H extends HashKind<H>> HashBuilder<InputStream, H> forInputStream(H kind) {
        return new DigestiveHashBuilder<>(MessageByteDigest.get(kind), inputStream2Bytes());
    }

    static <H extends HashKind<H>> HashBuilder<Bytes, H> forKind(H kind) {
        return new DigestiveHashBuilder<>(MessageByteDigest.get(kind), IDENTITY);
    }

    static Function<InputStream, Stream<Bytes>> inputStream2Bytes() {
        return is ->
            StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                    new BytesIterator(is),
                    IMMUTABLE & NONNULL
                ),
                false
            );
    }

    /// @return Hash kind
    H kind();

    /// Add items
    ///
    /// @param items Items tp add
    /// @return This builder
    default HashBuilder<T, H> hash(List<T> items) {
        items.forEach(this::hash);
        return this;
    }

    /// Add items
    ///
    /// @param items Items to add
    /// @return This builder
    default HashBuilder<T, H> hash(Stream<T> items) {
        items.forEach(this::hash);
        return this;
    }

    /// Add to the hash
    ///
    /// @param item Item to add
    HashBuilder<T, H> hash(T item);

    /// Get the hash, reset the underlying hasher.
    ///
    /// @return Hash
    Hash<H> build();

    /// @param transform Transformer for `R` to `T`
    /// @param <R>       Input type to new hasher
    /// @return New hasher that accepts  `R`
    <R> HashBuilder<R, H> map(Function<R, T> transform);

    /// @param transform Transformer for `R` to `Stream<T>`
    /// @return New hasher that accepts `R`
    <R> HashBuilder<R, H> flatMap(Function<R, Stream<T>> transform);

    final class BytesIterator implements Iterator<Bytes> {

        private final byte[] buffer = new byte[8192];

        private int read;

        private final InputStream inputStream;

        private BytesIterator(InputStream inputStream) {
            this.inputStream = requireNonNull(inputStream, "is");
            this.read = advance();
        }

        @Override
        public boolean hasNext() {
            return read >= 0;
        }

        @Override
        public Bytes next() {
            var next = new Bytes(buffer, 0, read);
            read = advance();
            return next;
        }

        private int advance() {
            try {
                return inputStream.read(buffer);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
