package com.github.kjetilv.uplift.hash;

import com.github.kjetilv.uplift.kernel.io.Bytes;

import java.io.InputStream;
import java.util.Base64;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public static Hash<K128> ofNullable(long l0, long l1) {
        return l0 == 0 && l1 == 0
            ? BLANK_128
            : of(l0, l1);
    }

    public static Hash<K256> ofNullable(long l0, long l1, long l2, long l3) {
        return l0 == 0 && l1 == 0 && l2 == 0 && l3 == 0
            ? BLANK_256
            : of(l0, l1, l2, l3);
    }

    public static Hash<K128> of(long l0, long l1) {
        return new H128(l0, l1);
    }

    public static Hash<K256> of(long l0, long l1, long l2, long l3) {
        return new H256(l0, l1, l2, l3);
    }

    @SuppressWarnings("unchecked")
    public static <H extends HashKind<H>> Hash<H> hash(byte[] bytes) {
        requireNonNull(bytes, "bytes");
        if (bytes.length == K128.byteCount()) {
            long[] ls = toLongs(bytes, new long[K128.longCount()]);
            return (Hash<H>) new H128(ls[0], ls[1]);
        }
        if (bytes.length == K256.byteCount()) {
            long[] ls = toLongs(bytes, new long[K256.longCount()]);
            return (Hash<H>) new H256(ls[0], ls[1], ls[2], ls[3]);
        }
        throw new IllegalStateException("Byte size for hash not recognized: " + bytes.length + " bytes");
    }

    @SuppressWarnings("unchecked")
    public static <H extends HashKind<H>> Hash<H> hash(String raw) {
        int length = requireNonNull(raw, "raw").length();
        if (length == K128.digestLength()) {
            long[] ls = toLongs(raw, new long[K128.longCount()]);
            return (Hash<H>) new H128(ls[0], ls[1]);
        }
        if (length == K256.digestLength()) {
            long[] ls = toLongs(raw, new long[K256.longCount()]);
            return (Hash<H>) new H256(ls[0], ls[1], ls[2], ls[3]);
        }
        throw new IllegalArgumentException("Malformed hash of length " + length + " not recognized: " + raw);
    }

    public static byte[] bytes(int i) {
        return intToBytes(i, 0, new byte[Integer.BYTES]);
    }

    public static byte[] bytes(long l) {
        return longToBytes(l, 0, new byte[Long.BYTES]);
    }

    public static int toInt(byte[] bs) {
        return (bs[0] & 0xFF) << 28 |
               (bs[1] & 0xFF) << 16 |
               (bs[2] & 0xFF) << 8 |
               bs[3] & 0xFF;
    }

    public static byte[] bytes(long l0, long l1) {
        byte[] bytes = new byte[16];
        longToBytes(l0, 0, bytes);
        longToBytes(l1, 8, bytes);
        return bytes;
    }

    public static byte[] bytes(long l0, long l1, long l2, long l3) {
        byte[] bytes = new byte[32];
        longToBytes(l0, 0, bytes);
        longToBytes(l1, 8, bytes);
        longToBytes(l2, 16, bytes);
        longToBytes(l3, 24, bytes);
        return bytes;
    }

    public static long bytesToLong(byte[] bytes, int start) {
        long lw = 0;
        for (int i = 0; i < 7; i++) {
            lw |= bytes[i + start] & 0xFF;
            lw <<= 8;
        }
        lw |= bytes[7 + start] & 0xFF;
        return lw;
    }

    @SuppressWarnings("SameParameterValue")
    public static byte[] intToBytes(int l, int index, byte[] bytes) {
        long w = l;
        for (int j = 3; j > 0; j--) {
            bytes[index + j] = (byte) (w & 0xFF);
            w >>= 8;
        }
        bytes[index] = (byte) (w & 0xFF);
        return bytes;
    }

    public static byte[] toBytes(long[] ls) {
        byte[] bytes = new byte[ls.length * Long.BYTES];
        for (int l = 0; l < ls.length; l++) {
            longToBytes(ls[l], l * 8, bytes);
        }
        return bytes;
    }

    public static byte[] longToBytes(long i, int index, byte[] bytes) {
        long w = i;
        for (int j = 7; j > 0; j--) {
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
        return new DigestiveHashBuilder<>(MessageByteDigest.get(kind), Hashes.inputStream2Bytes());
    }

    public static Function<Integer, byte[]> intToBytes() {
        return Hashes::bytes;
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
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < ls.length; j++) {
                ls[j] <<= 8;
                ls[j] |= bytes[i + j * 8] & 0xFF;
            }
        }
        return ls;
    }

    private static long[] toLongs(String raw, long[] ls) {
        String digest = raw
            .replace(GOOD_1, BAD_1)
            .replace(GOOD_2, BAD_2);
        byte[] decoded = DECODER.decode(digest);
        for (int l = 0; l < ls.length; l++) {
            ls[l] = bytesToLong(decoded, l * 8);
        }
        return ls;
    }

    private static final class BytesIterator implements Iterator<Bytes> {

        private final byte[] buffer = new byte[8192];

        private int read;

        private final InputStream is;

        private BytesIterator(InputStream is) {
            this.is = requireNonNull(is, "is");
            this.read = advance();
        }

        @Override
        public boolean hasNext() {
            return read >= 0;
        }

        @Override
        public Bytes next() {
            Bytes next = new Bytes(buffer, 0, read);
            read = advance();
            return next;
        }

        private int advance() {
            try {
                return is.read(buffer);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
