package com.github.kjetilv.uplift.hash;

import module java.base;
import module uplift.util;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static com.github.kjetilv.uplift.hash.HashKind.K256;
import static com.github.kjetilv.uplift.hash.Hashes.*;
import static java.nio.charset.StandardCharsets.US_ASCII;

/// A 256-bit hash, exposed as four longs.
public sealed interface Hash<H extends HashKind<H>> extends Comparable<Hash<H>> permits Hash.H128, Hash.H256 {

    String LPAR = "⟨";

    String RPAR = "⟩";

    static <H extends HashKind<H>> String toShortHashString(List<Hash<H>> hashes) {
        return hashes.stream()
            .map(Hash::toShortString)
            .collect(Collectors.joining("—"));
    }

    /// @return Unique [digest-length][#digestLength()] string representation
    default String digest() {
        var bytes = longsToBytes(ls());
        var base64 = new String(ENCODER.encode(bytes), US_ASCII);
        var padding = kind().digest().padding();
        if (base64.length() == kind().digest().length() + padding.length() && base64.endsWith(padding)) {
            return base64.substring(0, digestLength())
                .replace(BAD_1, GOOD_1)
                .replace(BAD_2, GOOD_2);
        }
        throw new IllegalStateException("Unusual hash: " + base64);
    }

    default Bytes toBytes() {
        return Bytes.from(bytes());
    }

    /// @return Byte representation of the id
    default byte[] bytes() {
        var ls = ls();
        var bytes = new byte[ls.length * 8];
        for (var l = 0; l < ls.length; l++) {
            for (var i = 0; i < 8; i++) {
                bytes[l * 8 + i] = (byte) (ls[l] >>> 8 * (7 - i));
            }
        }
        return bytes;
    }

    default String toShortString() {
        return LPAR + digest().substring(0, 6) + RPAR;
    }

    default String toLongString() {
        return LPAR + digest() + RPAR;
    }

    @Override
    default int compareTo(Hash<H> o) {
        if (equals(o)) {
            return 0;
        }
        if (o.getClass() == getClass()) {
            return Arrays.compare(ls(), o.ls());
        }
        throw new ClassCastException(this + " is not comparable to " + o);
    }

    default boolean isBlank() {
        if (this == BLANK_128 || this == BLANK_256) {
            return true;
        }
        for (var l : ls()) {
            if (l != 0) {
                return false;
            }
        }
        return true;
    }

    default byte byteAt(int index) {
        return bytes()[index];
    }

    default String toStringCustom(int length) {
        if (length < 3) {
            throw new IllegalArgumentException(this + ": Invalid length: " + length + ", should be >= 2");
        }
        if (length > digestLength() + 2) {
            throw new IllegalArgumentException(this + ": Invalid length: " + length + ", should <= " + digestLength() + 2);
        }
        return LPAR + digest().substring(0, length - 2) + RPAR;
    }

    default String defaultToString() {
        var fifth = kind().digest().length() / 5;
        return LPAR + digest().substring(0, Math.max(10, fifth)) + RPAR;
    }

    H kind();

    int digestLength();

    /// The longs,
    ///
    /// @return Longs
    long[] ls();

    record H128(long l0, long l1) implements Hash<K128> {

        @Override
        public long[] ls() {
            return new long[] {l0, l1};
        }

        @Override
        public K128 kind() {
            return K128;
        }

        @Override
        public int digestLength() {
            return 22;
        }

        @Override
        public String toString() {
            return defaultToString();
        }
    }

    record H256(long l0, long l1, long l2, long l3) implements Hash<K256> {

        @Override
        public K256 kind() {
            return K256;
        }

        @Override
        public int digestLength() {
            return 43;
        }

        @Override
        public long[] ls() {
            return new long[] {l0, l1, l2, l3};
        }

        @Override
        public String toString() {
            return defaultToString();
        }
    }
}
