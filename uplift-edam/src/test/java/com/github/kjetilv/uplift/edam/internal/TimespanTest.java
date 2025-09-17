package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.patterns.Spanning;
import com.github.kjetilv.uplift.edam.patterns.Timespan;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimespanTest {

    @Test
    void spans() {
        Instant t = Instant.EPOCH;
        Timespan timespan1 = Timespan.of(
            spanning(t),
            spanning(t.plusSeconds(20))
        );
        assertEquals(20, timespan1.duration().getSeconds());

        Timespan timespan2 = Timespan.of(
            spanning(t.plusSeconds(60)),
            spanning(t.plusSeconds(90))
        );
        assertEquals(30, timespan2.duration().getSeconds());

        Timespan timespan3 = Timespan.of(timespan1, timespan2);
        assertEquals(90, timespan3.duration().getSeconds());
    }

    private static Spanning spanning(Instant time) {
        return () -> new Timespan(time);
    }

}