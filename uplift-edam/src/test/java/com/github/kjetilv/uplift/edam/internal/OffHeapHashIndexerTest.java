package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OffHeapHashIndexerTest {

    @Test
    void exchange() {
        try (
            Arena arena = Arena.ofConfined()
        ) {
            OffHeapIndexer128 indexer = new OffHeapIndexer128(
                arena, InternalFactory.JAVA, 10
            );
            Hash<K128> h0 = HashKind.K128.random();
            Hash<K128> h1 = HashKind.K128.random();

            assertEquals(indexer.exchange(h0), indexer.exchange(h0));
            assertEquals(indexer.exchange(h1), indexer.exchange(h1));

            assertTrue(indexer.exchange(h0) < indexer.limit());
            assertTrue(indexer.exchange(h1) < indexer.limit());
        }
    }

    @Test
    void fill() {
        try (
            Arena arena = Arena.ofConfined()
        ) {
            OffHeapIndexer128 indexer = new OffHeapIndexer128(
                arena, InternalFactory.JAVA, 64_000
            );
            Set<Long> ls = new HashSet<>();
            for (int i = 0; i < 64_000; i++) {
                try {
                    Hash<K128> hash = HashKind.K128.random();
                    long index;
                    try {
                        index = indexer.exchange(hash);
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed at index " + i, e);
                    }
                    assertEquals(hash, indexer.exchange(index));
                    assertEquals(index, indexer.exchange(hash));
                    assertEquals(i, ls.size());
                    ls.add(index);
                } catch (Throwable e) {
                    Assertions.fail("Failed at index " + i + ": " + e.getMessage(), e);
                }
            }
        }
    }
}