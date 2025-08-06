package com.github.kjetilv.uplift.hash;

import java.util.UUID;

public sealed interface HashKind<H extends HashKind<H>> {

    K128 K128 = new K128();

    K256 K256 = new K256();

    default int byteCount() {
        return bits() / 8;
    }

    default int longCount() {
        return bits() / 64;
    }

    String algorithm();

    int bits();

    int digestLength();

    String digestPadding();

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
            this(
                "MD5",
                128,
                22,
                "==",
                Hashes.of(0L, 0L)
            );
        }

        @Override
        public Hash<K128> random() {
            UUID u = UUID.randomUUID();
            return Hashes.of(u.getMostSignificantBits(), u.getLeastSignificantBits());
        }
    }

    record K256(
        String algorithm,
        int bits,
        int digestLength,
        String digestPadding,
        Hash<K256> blank
    ) implements HashKind<K256> {

        private K256() {
            this(
                "SHA3-256",
                256,
                43,
                "=",
                Hashes.of(0L, 0L, 0L, 0L)
            );
        }

        @Override
        public Hash<K256> random() {
            UUID u0 = UUID.randomUUID();
            UUID u1 = UUID.randomUUID();
            return Hashes.of(
                u0.getMostSignificantBits(), u0.getLeastSignificantBits(),
                u1.getMostSignificantBits(), u1.getLeastSignificantBits()
            );
        }
    }
}
