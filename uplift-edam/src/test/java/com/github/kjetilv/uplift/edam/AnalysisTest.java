package com.github.kjetilv.uplift.edam;

import com.github.kjetilv.uplift.edam.patterns.HashPattern;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.edam.patterns.PatternMatch;
import com.github.kjetilv.uplift.edam.patterns.PatternOccurrence;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static java.time.Instant.EPOCH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalysisTest {

    public static final Hash<K128> H0 = K128.random();

    public static final Hash<K128> H1 = K128.random();

    public static final Hash<K128> H2 = K128.random();

    @Test
    void counts() {
        Occurrence<K128> occ0 = new Occurrence<>(ep(0), H0);
        Occurrence<K128> occ1 = new Occurrence<>(ep(1), H2);
        Occurrence<K128> occ2 = new Occurrence<>(ep(2), H2);
        Occurrence<K128> occ3 = new Occurrence<>(ep(3), H0);
        Occurrence<K128> occ4 = new Occurrence<>(ep(4), H2);
        HashPattern<K128> hashPattern1 = new HashPattern<>(H0, H2, H0, H2);
        HashPattern<K128> hashPattern2 = new HashPattern<>(H2);
        Analysis.Multiple<K128> multiple = new Analysis.Multiple<>(
            occ4,
            List.of(
                new PatternMatch<>(
                    hashPattern1,
                    List.of(
                        new PatternOccurrence<>(hashPattern1, List.of(occ0, occ1)),
                        new PatternOccurrence<>(hashPattern1, List.of(occ3, occ4))
                    )
                ),
                new PatternMatch<>(
                    hashPattern2,
                    List.of(
                        new PatternOccurrence<>(hashPattern2, List.of(occ1)),
                        new PatternOccurrence<>(hashPattern2, List.of(occ2)),
                        new PatternOccurrence<>(hashPattern2, List.of(occ4))
                    )
                )
            )
        );
        assertEquals(H2, multiple.trigger().hash());
        assertEquals(3, multiple.triggerHashCount());
        assertEquals(2, multiple.count(H0));
        assertEquals(0, multiple.count(H1));

        assertEquals(3, multiple.triggerHashCount());
        assertEquals(3, multiple.count(H2));
        assertEquals(2, multiple.count(H0));
        assertEquals(5, multiple.count(null));
        assertEquals(2, multiple.distinctOccurrencesCount());
    }

    private static Instant ep(int s) {
        return EPOCH.plusSeconds(s);
    }
}