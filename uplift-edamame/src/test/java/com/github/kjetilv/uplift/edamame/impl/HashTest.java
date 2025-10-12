package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.Hashes;
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
}