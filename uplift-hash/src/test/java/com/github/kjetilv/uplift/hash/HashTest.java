package com.github.kjetilv.uplift.hash;

import com.github.kjetilv.uplift.hash.HashKind.K128;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HashTest {

    @Test
    void string() {
        String string = HashKind.K128.random().toString();
        assertEquals(12, string.length());
    }

    @Test
    void byteAt128() {
        Hash<K128> random = HashKind.K128.random();
        byte[] bytes = random.bytes();
        for (int i = 0; i < 16; i++) {
            int finalI = i;
            assertEquals(
                bytes[i], random.byteAt(i),
                () -> "Disagree on " + finalI
            );
        }
    }

    @Test
    void byteAt256() {
        Hash<HashKind.K256> random = HashKind.K256.random();
        byte[] bytes = random.bytes();
        for (int i = 0; i < 32; i++) {
            int finalI = i;
            assertEquals(
                bytes[i], random.byteAt(i),
                () -> "Disagree on " + finalI
            );
        }
    }

    @Test
    void backAndForth128() {
        assertBackAndForth(HashKind.K128);
    }

    @Test
    void backAndForth256() {
        assertBackAndForth(HashKind.K256);
    }

    private static <H extends HashKind<H>> void assertBackAndForth(H kind) {
        Hash<H> random = kind.random();
        String digest = random.digest();
        Hash<H> hash = Hashes.hash(digest);
        assertEquals(random, hash, "Hashing the same digest twice should produce the same hash");
        assertEquals(digest, hash.digest(), "Hashing the same digest twice should produce the same hash");
    }
}