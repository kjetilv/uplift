package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.patterns.HashPattern;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
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
        var h1 = K128.random();
        var h2 = K128.random();
        var h3 = K128.random();
        var hashPattern = new HashPattern<K128>(h1, h2, h3);
        var occ1 = new PatternOccurrence<K128>(hashPattern);
        assertTrue(occ1.matchesNext(h1));
        assertFalse(occ1.matchesNext(h2));
        assertFalse(occ1.matchesNext(h3));

        var to1 = new Occurrence<K128>(Instant.now(), h1);
        var to2 = new Occurrence<K128>(Instant.now(), h2);
        var to3 = new Occurrence<K128>(Instant.now(), h3);

        assertTrue(occ1.matchingOccurrence(to1).isPresent());
        assertTrue(occ1.matchingOccurrence(to2).isEmpty());
        assertTrue(occ1.matchingOccurrence(to3).isEmpty());

        var occ2 = occ1.matchingOccurrence(to1).get();
        assertFalse(occ2.matchesNext(h1));
        assertTrue(occ2.matchesNext(h2));
        assertFalse(occ2.matchesNext(h3));

        var done = occ2.matchingOccurrence(to2)
            .flatMap(o ->
                o.matchingOccurrence(to3)).get();

        assertTrue(done.match());
    }
}