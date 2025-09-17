package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.edam.patterns.Pattern;
import com.github.kjetilv.uplift.edam.patterns.PatternOccurrence;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class SequenceOccurrenceOccTest {

    @Test
    void shouldProgress() {
        Hash<K128> h1 = K128.random();
        Hash<K128> h2 = K128.random();
        Hash<K128> h3 = K128.random();
        Pattern<K128> pattern = new Pattern<>(h1, h2, h3);
        PatternOccurrence<K128> occ1 = new PatternOccurrence<>(pattern);
        assertTrue(occ1.matchesNext(h1));
        assertFalse(occ1.matchesNext(h2));
        assertFalse(occ1.matchesNext(h3));

        Occurrence<K128> to1 = new Occurrence<>(Instant.now(), h1);
        Occurrence<K128> to2 = new Occurrence<>(Instant.now(), h2);
        Occurrence<K128> to3 = new Occurrence<>(Instant.now(), h3);

        assertTrue(occ1.matchingOccurrence(to1).isPresent());
        assertTrue(occ1.matchingOccurrence(to2).isEmpty());
        assertTrue(occ1.matchingOccurrence(to3).isEmpty());

        PatternOccurrence<K128> occ2 = occ1.matchingOccurrence(to1).get();
        assertFalse(occ2.matchesNext(h1));
        assertTrue(occ2.matchesNext(h2));
        assertFalse(occ2.matchesNext(h3));

        PatternOccurrence<K128> done = occ2.matchingOccurrence(to2)
            .flatMap(o ->
                o.matchingOccurrence(to3)).get();

        assertTrue(done.match());
    }
}