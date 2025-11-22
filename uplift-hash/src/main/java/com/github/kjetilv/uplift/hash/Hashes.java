package com.github.kjetilv.uplift.hash;

import module java.base;
import com.github.kjetilv.uplift.util.Bytes;

import static com.github.kjetilv.uplift.hash.Hash.H128;
import static com.github.kjetilv.uplift.hash.Hash.H256;
import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static com.github.kjetilv.uplift.hash.HashKind.K256;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;

@SuppressWarnings("unused")
public final class Hashes {

    public static final Hash<K128> BLANK_128 = of(0L, 0L);

    public static final Hash<K256> BLANK_256 = of(0L, 0L, 0L, 0L);

    public static Function<InputStream, Stream<Bytes>> inputStream2Bytes() {
        return is ->
            StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                    new BytesIterator(is),
                    IMMUTABLE & NONNULL
                ),
                false
            );
    }

    public static Hash<K128> ofNullable(long long0, long long1) {
        return long0 == 0 && long1 == 0
            ? BLANK_128
            : of(long0, long1);
    }

    public static Hash<K256> ofNullable(long long0, long long1, long long2, long long3) {
        return long0 == 0 && long1 == 0 && long2 == 0 && long3 == 0
            ? BLANK_256
            : of(long0, long1, long2, long3);
    }

    public static Hash<K128> of(long long0, long long1) {
        return new H128(long0, long1);
    }

    public static Hash<K256> of(long long0, long long1, long long2, long long3) {
        return new H256(long0, long1, long2, long3);
    }

    @SuppressWarnings("unchecked")
    public static <H extends HashKind<H>> Hash<H> hash(byte[] bytes) {
        requireNonNull(bytes, "bytes");
        if (bytes.length == K128.byteCount()) {
            var ls = ByteUtils.toLongs(bytes, new long[K128.longCount()]);
            return (Hash<H>) new H128(ls[0], ls[1]);
        }
        if (bytes.length == K256.byteCount()) {
            var ls = ByteUtils.toLongs(bytes, new long[K256.longCount()]);
            return (Hash<H>) new H256(ls[0], ls[1], ls[2], ls[3]);
        }
        throw new IllegalStateException("Byte size for hash not recognized: " + bytes.length + " bytes");
    }

    @SuppressWarnings("unchecked")
    public static <H extends HashKind<H>> Hash<H> hash(String raw) {
        var length = requireNonNull(raw, "raw").length();
        if (length == K128.digestLength()) {
            var ls = ByteUtils.toLongs(raw, new long[K128.longCount()]);
            return (Hash<H>) new H128(ls[0], ls[1]);
        }
        if (length == K256.digestLength()) {
            var ls = ByteUtils.toLongs(raw, new long[K256.longCount()]);
            return (Hash<H>) new H256(ls[0], ls[1], ls[2], ls[3]);
        }
        throw new IllegalArgumentException("Malformed hash of length " + length + " not recognized: " + raw);
    }

    public static Bytes intToBytes(int i) {
        return Bytes.from(intBytes(i));
    }

    public static byte[] intBytes(int i) {
        return ByteUtils.intToBytes(i, 0, new byte[Integer.BYTES]);
    }

    public static byte[] longBytes(long l) {
        return ByteUtils.longToBytes(l, 0, new byte[Long.BYTES]);
    }

    public static <H extends HashKind<H>> HashBuilder<byte[], H> bytesBuilder(H kind) {
        return hashBuilder(kind).map(Bytes::from);
    }

    public static <H extends HashKind<H>> HashBuilder<Bytes, H> hashBuilder(H kind) {
        return new DigestiveHashBuilder<>(MessageByteDigest.get(kind), IDENTITY);
    }

    public static <H extends HashKind<H>> HashBuilder<InputStream, H> inputStreamHashBuilder(H kind) {
        return new DigestiveHashBuilder<>(MessageByteDigest.get(kind), inputStream2Bytes());
    }

    public static Function<Integer, byte[]> intToBytes() {
        return Hashes::intBytes;
    }

    private Hashes() {
    }

    static final char GOOD_1 = '-';

    static final char GOOD_2 = '_';

    static final char BAD_2 = '+';

    static final char BAD_1 = '/';

    static final String PADDING = "==";

    private static final Function<Bytes, Stream<Bytes>> IDENTITY = Stream::of;

    private static final class BytesIterator implements Iterator<Bytes> {

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
