package com.github.kjetilv.uplift.hash;

import module java.base;

/// A hash kind is either
///
/// * [#K128], a 16-byte hash using {@link HashKind.K128#ALGORITHM MD5} hashing, or
/// * [#K256], a 32 byte one using {@link HashKind.K256#ALGORITHM SHA-256} hashing
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

    /// @return True iff the provided base64 string matches this kind's {@link Hash#digest() digest} format
    default boolean isDigest(String base64) {
        return base64.length() == digestLength() + paddingLength() && base64.endsWith(digestPadding());
    }

    default int totalDigestLength() {
        return digestLength() + paddingLength();
    }

    /// @return Length of padding in {@link Hash#digest() digests}
    default int paddingLength() {
        return digestPadding().length();
    }

    /// @return Length of {@link Hash#digest() digests}, not including padding
    int digestLength();

    /// @return Padding string for {@link Hash#digest() digests}
    String digestPadding();

    /// @return Name of algorithm used for hashing
    String algorithm();

    /// @return The number of bits in the hash
    int bits();

    /// @return The canonical all-zeroes instance
    Hash<H> blank();

    /// @return A random hash
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
            return Hash.of(
                u.getMostSignificantBits(),
                u.getLeastSignificantBits()
            );
        }

        private static final Hash<K128> BLANK_128 = Hash.of(0L, 0L);

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
            return Hash.of(
                u0.getMostSignificantBits(),
                u0.getLeastSignificantBits(),
                u1.getMostSignificantBits(),
                u1.getLeastSignificantBits()
            );
        }

        private static final Hash<K256> BLANK_256 = Hash.of(0L, 0L, 0L, 0L);

        private static final String ALGORITHM = "SHA3-256";
    }
}
