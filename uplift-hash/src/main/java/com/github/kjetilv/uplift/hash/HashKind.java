package com.github.kjetilv.uplift.hash;

import module java.base;

/// A hash kind is either
///
/// * [#K128], a 16-byte hash using MD5 hashing, or
/// * [#K256], a 32 byte one using SHA3-256 hashing
///
public sealed interface HashKind<K extends HashKind<K>> {

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
    Hash<K> blank();

    /// @return Hash from data input
    Hash<K> from(DataInput dataInput);

    /// @return A random hash
    Hash<K> random();

    record K128(
        String algorithm,
        int bits,
        int digestLength,
        String digestPadding,
        Hash<K128> blank
    ) implements HashKind<K128> {

        private K128() {
            this("MD5", 128, 22, "==", BLANK_128);
        }

        public Hash<K128> fromUuid(String uuidString) {
            return fromUuid(UUID.fromString(uuidString));
        }

        public Hash<K128> fromUuid(UUID uuid) {
            return of(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
        }

        @Override
        public Hash<K128> from(DataInput di) {
            try {
                return of(di.readLong(), di.readLong());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read" + this + " from " + di, e);
            }
        }

        @Override
        public Hash<K128> random() {
            var u = UUID.randomUUID();
            return K128.of(
                u.getMostSignificantBits(),
                u.getLeastSignificantBits()
            );
        }

        public Hash<K128> of(long l0, long l1) {
            return new Hash.H128(l0, l1);
        }

        private static final Hash<K128> BLANK_128 = K128.of(0L, 0L);
    }

    record K256(
        String algorithm,
        int bits,
        int digestLength,
        String digestPadding,
        Hash<K256> blank
    ) implements HashKind<K256> {

        private K256() {
            this("SHA3-256", 256, 43, "=", BLANK_256);
        }

        @Override
        public Hash<K256> from(DataInput di) {
            try {
                return of(di.readLong(), di.readLong(), di.readLong(), di.readLong());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read" + this + " from " + di, e);
            }
        }

        @Override
        public Hash<K256> random() {
            var u0 = UUID.randomUUID();
            var u1 = UUID.randomUUID();
            return K256.of(
                u0.getMostSignificantBits(),
                u0.getLeastSignificantBits(),
                u1.getMostSignificantBits(),
                u1.getLeastSignificantBits()
            );
        }

        public Hash<K256> of(long l0, long l1, long l2, long l3) {
            return new Hash.H256(l0, l1, l2, l3);
        }

        private static final Hash<K256> BLANK_256 = K256.of(0L, 0L, 0L, 0L);
    }
}
