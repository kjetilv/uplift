package com.github.kjetilv.uplift.hash;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.util.UUID;

import static com.github.kjetilv.uplift.hash.HashKind.*;
import static com.github.kjetilv.uplift.hash.HashKind.K256;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HashTest {

    @Test
    void testNull() {
        assertEquals(K128.blank(), K128.of(0L, 0L));
    }

    @Test
    void testHash() {
        var hash = K128.of(0L, 234L);
        assertEquals("⟨AAAAAAAA⟩", hash.toString());
    }

    @Test
    void string() {
        var string = K128.random().toString();
        assertEquals(10, string.length());
    }

    @Test
    void byteAt128() {
        var random = K128.random();
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
        var random = K256.random();
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
        assertBackAndForth(K128);
    }

    @Test
    void backAndForth256() {
        assertBackAndForth(K256);
    }

    @Test
    void uuidsAsString() {
        var string = UUID.randomUUID().toString();
        var hash = K128.fromUuid(string);
        assertEquals(string, hash.asUuid().toString());
    }

    @Test
    void uuidsAsDigests() {
        var string = UUID.randomUUID().toString();
        var hash = K128.fromUuid(string);

        var digest = hash.digest();
        var hash1 = Hash.from(digest);

        assertEquals(string,hash1.asUuid().toString());
    }

    @Test
    void io() {
        var hash = K256.random();
        DataInput dataInput = new DataInputStream(new ByteArrayInputStream(hash.bytes()));
        assertEquals(hash, K256.from(dataInput));
    }

    private static <H extends HashKind<H>> void assertBackAndForth(H kind) {
        var random = kind.random();
        var digest = random.digest();
        Hash<H> hash = Hash.from(digest);
        assertEquals(random, hash, "Hashing the same digest twice should produce the same hash");
        assertEquals(digest, hash.digest(), "Hashing the same digest twice should produce the same hash");
    }
}