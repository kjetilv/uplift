package com.github.kjetilv.uplift.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HashTest {

    @Test
    void testNull() {
        assertEquals(HashKind.K128.blank(), Hashes.of(0L, 0L));
    }

    @Test
    void testHash() {
        var hash = Hashes.of(0L, 234L);
        assertEquals("⟨AAAAAAAAAA⟩", hash.toString());
    }

    @Test
    void string() {
        var string = HashKind.K128.random().toString();
        assertEquals(12, string.length());
    }

    @Test
    void byteAt128() {
        var random = HashKind.K128.random();
        var bytes = random.bytes();
        for (var i = 0; i < 16; i++) {
            var finalI = i;
            assertEquals(
                bytes[i], random.byteAt(i),
                () -> "Disagree on " + finalI
            );
        }
    }

    @Test
    void byteAt256() {
        var random = HashKind.K256.random();
        var bytes = random.bytes();
        for (var i = 0; i < 32; i++) {
            var finalI = i;
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
        var random = kind.random();
        var digest = random.digest();
        Hash<H> hash = Hashes.hash(digest);
        assertEquals(random, hash, "Hashing the same digest twice should produce the same hash");
        assertEquals(digest, hash.digest(), "Hashing the same digest twice should produce the same hash");
    }
}