package com.github.kjetilv.uplift.hash;

import module java.base;

import static com.github.kjetilv.uplift.hash.Hashes.*;
import static com.github.kjetilv.uplift.hash.Hashes.GOOD_2;

/// A hash kind is either
///
/// * [#K128], 16-byte hash, or
/// * [#K256], a 32 byte one
///
public sealed interface HashKind<H extends HashKind<H>> {

    /// 128-bit hash
    K128 K128 = new K128();

    /// 256-bit hash
    K256 K256 = new K256();

    /// Number of bytes in the hash kind
    default int byteCount() {
        return bits() / 8;
    }

    /// Number of longs in the hash kind
    default int longCount() {
        return bits() / 64;
    }

    default boolean isDigest(String base64) {
        return base64.length() == digestLength() + paddingLength() && base64.endsWith(digestPadding());
    }

    default int paddingLength() {
        return digestPadding().length();
    }

    int digestLength();

    String digestPadding();

    /// @return Name of algorithm used for hashing
    String algorithm();

    /// @return The number of bits in the hash
    int bits();

    Hash<H> blank();

    Hash<H> random();

    record K128(
        String algorithm,
        int bits,
        int digestLength,
        String digestPadding,
        Hash<K128> blank
    ) implements HashKind<K128> {

        private K128() {
            this(ALGORITHM, 128, 22, "==", BLANK_128);
        }

        @Override
        public Hash<K128> random() {
            var u = UUID.randomUUID();
            return Hashes.of(
                u.getMostSignificantBits(),
                u.getLeastSignificantBits()
            );
        }

        private static final Hash<K128> BLANK_128 = Hashes.of(0L, 0L);

        private static final String ALGORITHM = "MD5";
    }

    record K256(
        String algorithm,
        int bits,
        int digestLength,
        String digestPadding,
        Hash<K256> blank
    ) implements HashKind<K256> {

        private K256() {
            this(ALGORITHM, 256, 43, "=", BLANK_256);
        }

        @Override
        public Hash<K256> random() {
            var u0 = UUID.randomUUID();
            var u1 = UUID.randomUUID();
            return Hashes.of(
                u0.getMostSignificantBits(),
                u0.getLeastSignificantBits(),
                u1.getMostSignificantBits(),
                u1.getLeastSignificantBits()
            );
        }

        private static final Hash<K256> BLANK_256 = Hashes.of(0L, 0L, 0L, 0L);

        private static final String ALGORITHM = "SHA3-256";
    }
}
