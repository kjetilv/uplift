package com.github.kjetilv.uplift.edam.patterns;

import com.github.kjetilv.uplift.hash.Hash;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static java.time.Instant.EPOCH;
import static org.junit.jupiter.api.Assertions.*;

class PatternMatchTest {

    @Test
    void counts() {
        PatternMatch<K128> patternMatch = new PatternMatch<>(
            pattern,
            List.of(
                pattern.at(ep(0), ep(1), ep(2)),
                pattern.at(ep(3), ep(4), ep(5))
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
        PatternMatch<K128> patternMatch = new PatternMatch<>(
            pattern,
            List.of(
                pattern.at(ep(0), ep(1)),
                pattern.at(ep(3), ep(4), ep(5))
            )
        );
        assertFalse(patternMatch.match());

        assertFalse(patternMatch.occurrences().getFirst().match());
        assertTrue(patternMatch.occurrences().getFirst().matchesNext(h2));

        assertTrue(patternMatch.occurrences().getLast().match());
    }

    private final static Hash<K128> h0 = K128.random();

    private final static Hash<K128> h1 = K128.random();

    private final static Hash<K128> h2 = K128.random();

    private final static Pattern<K128> pattern = new Pattern<>(h0, h1, h2);

    private static Instant ep(int s) {
        return EPOCH.plusSeconds(s);
    }
}