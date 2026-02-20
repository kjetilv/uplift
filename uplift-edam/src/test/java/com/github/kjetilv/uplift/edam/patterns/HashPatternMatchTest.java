package com.github.kjetilv.uplift.edam.patterns;

import com.github.kjetilv.uplift.hash.Hash;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static java.time.Instant.EPOCH;
import static org.junit.jupiter.api.Assertions.*;

class HashPatternMatchTest {

    @Test
    void counts() {
        var patternMatch = new PatternMatch<K128>(
            HASH_PATTERN,
            List.of(
                HASH_PATTERN.at(ep(0), ep(1), ep(2)),
                HASH_PATTERN.at(ep(3), ep(4), ep(5))
            )
        );

        assertEquals(2, patternMatch.count(h2));
        assertEquals(new Timespan(ep(0), ep(5)), patternMatch.timespan());
        assertEquals(ep(0), patternMatch.startTime());
        assertEquals(ep(5), patternMatch.lastTime());
        assertTrue(patternMatch.match());
    }

    @Test
    void nonMatch() {
        var patternMatch = new PatternMatch<K128>(
            HASH_PATTERN,
            List.of(
                HASH_PATTERN.at(ep(0), ep(1)),
                HASH_PATTERN.at(ep(3), ep(4), ep(5))
            )
        );
        assertFalse(patternMatch.match());

        assertFalse(patternMatch.occurrences().getFirst().match());
        assertTrue(patternMatch.occurrences().getFirst().matchesNext(h2));

        assertTrue(patternMatch.occurrences().getLast().match());
    }

    private static final Hash<K128> h0 = K128.random();

    private static final Hash<K128> h1 = K128.random();

    private static final Hash<K128> h2 = K128.random();

    private static final HashPattern<K128> HASH_PATTERN = new HashPattern<>(h0, h1, h2);

    private static Instant ep(int s) {
        return EPOCH.plusSeconds(s);
    }
}