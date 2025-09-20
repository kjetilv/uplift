package com.github.kjetilv.uplift.hash;

import java.util.UUID;

/// It's a kind hash
public sealed interface HashKind<H extends HashKind<H>> {

    K128 K128 = new K128();

    K256 K256 = new K256();

    default int byteCount() {
        return bits() / 8;
    }

    default int longCount() {
        return bits() / 64;
    }

    /**
     * @return Name of algorithm used for hashing
     */
    String algorithm();

    /**
     * @return The number of bits in the hash
     */
    int bits();

    Digest digest();

    Hash<H> blank();

    Hash<H> random();

    record Digest(int length, String padding) {
    }

    record K128(
        String algorithm,
        int bits,
        Digest digest,
        Hash<K128> blank
    ) implements HashKind<K128> {

        private K128() {
            this(ALGORITHM, 128, new Digest(22, "=="), BLANK_128);
        }

        @Override
        public Hash<K128> random() {
            UUID u = UUID.randomUUID();
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
        Digest digest,
        Hash<K256> blank
    ) implements HashKind<K256> {

        private K256() {
            this(ALGORITHM, 256, new Digest(43, "="), BLANK_256);
        }

        @Override
        public Hash<K256> random() {
            UUID u0 = UUID.randomUUID();
            UUID u1 = UUID.randomUUID();
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
