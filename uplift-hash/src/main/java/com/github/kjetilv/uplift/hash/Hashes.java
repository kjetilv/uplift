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

    public static final Base64.Encoder ENCODER = Base64.getEncoder();

    public static final Base64.Decoder DECODER = Base64.getDecoder();

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
            var ls = toLongs(bytes, new long[K128.longCount()]);
            return (Hash<H>) new H128(ls[0], ls[1]);
        }
        if (bytes.length == K256.byteCount()) {
            var ls = toLongs(bytes, new long[K256.longCount()]);
            return (Hash<H>) new H256(ls[0], ls[1], ls[2], ls[3]);
        }
        throw new IllegalStateException("Byte size for hash not recognized: " + bytes.length + " bytes");
    }

    @SuppressWarnings("unchecked")
    public static <H extends HashKind<H>> Hash<H> hash(String raw) {
        var length = requireNonNull(raw, "raw").length();
        if (length == K128.digest().length()) {
            var ls = toLongs(raw, new long[K128.longCount()]);
            return (Hash<H>) new H128(ls[0], ls[1]);
        }
        if (length == K256.digest().length()) {
            var ls = toLongs(raw, new long[K256.longCount()]);
            return (Hash<H>) new H256(ls[0], ls[1], ls[2], ls[3]);
        }
        throw new IllegalArgumentException("Malformed hash of length " + length + " not recognized: " + raw);
    }

    public static Bytes intToBytes(int i) {
        return Bytes.from(intBytes(i));
    }

    public static byte[] intBytes(int i) {
        return intToBytes(i, 0, new byte[Integer.BYTES]);
    }

    public static byte[] longBytes(long l) {
        return longToBytes(l, 0, new byte[Long.BYTES]);
    }

    public static int bytesToInt(byte[] bytes) {
        return (bytes[0] & 0xFF) << 28 |
               (bytes[1] & 0xFF) << 16 |
               (bytes[2] & 0xFF) << 8 |
               bytes[3] & 0xFF;
    }

    public static byte[] longBytes(long long0, long long1) {
        var bytes = new byte[16];
        longToBytes(long0, 0, bytes);
        longToBytes(long1, 8, bytes);
        return bytes;
    }

    public static byte[] longBytes(long long0, long long1, long long2, long long3) {
        var bytes = new byte[32];
        longToBytes(long0, 0, bytes);
        longToBytes(long1, 8, bytes);
        longToBytes(long2, 16, bytes);
        longToBytes(long3, 24, bytes);
        return bytes;
    }

    public static long bytesToLong(byte[] bytes, int start) {
        long lw = 0;
        for (var i = 0; i < 7; i++) {
            lw |= bytes[i + start] & 0xFF;
            lw <<= 8;
        }
        lw |= bytes[7 + start] & 0xFF;
        return lw;
    }

    @SuppressWarnings("SameParameterValue")
    public static byte[] intToBytes(int l, int index, byte[] bytes) {
        long w = l;
        for (var j = 3; j > 0; j--) {
            bytes[index + j] = (byte) (w & 0xFF);
            w >>= 8;
        }
        bytes[index] = (byte) (w & 0xFF);
        return bytes;
    }

    public static byte[] longsToBytes(long[] longs) {
        var bytes = new byte[longs.length * Long.BYTES];
        for (var l = 0; l < longs.length; l++) {
            longToBytes(longs[l], l * 8, bytes);
        }
        return bytes;
    }

    public static byte[] longToBytes(long i, int index, byte[] bytes) {
        var w = i;
        for (var j = 7; j > 0; j--) {
            bytes[index + j] = (byte) (w & 0xFF);
            w >>= 8;
        }
        bytes[index] = (byte) (w & 0xFF);
        return bytes;
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

    private static long[] toLongs(byte[] bytes, long[] ls) {
        for (var i = 0; i < 8; i++) {
            for (var j = 0; j < ls.length; j++) {
                ls[j] <<= 8;
                ls[j] |= bytes[i + j * 8] & 0xFF;
            }
        }
        return ls;
    }

    private static long[] toLongs(String raw, long[] ls) {
        var digest = raw
            .replace(GOOD_1, BAD_1)
            .replace(GOOD_2, BAD_2);
        var decoded = DECODER.decode(digest);
        for (var l = 0; l < ls.length; l++) {
            ls[l] = bytesToLong(decoded, l * 8);
        }
        return ls;
    }

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
