package com.github.kjetilv.uplift.kernel.util;

import com.github.kjetilv.uplift.util.OnDemand;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OnDemandTest {

    @SuppressWarnings("MagicNumber")
    @Test
    void test() {
        var epo = Instant.EPOCH;
        var times = Arrays.asList(
            epo,
            epo.plusSeconds(10),
            epo.plusSeconds(20),
            epo.plusSeconds(30),
            epo.plusSeconds(40),
            epo.plusSeconds(50),
            epo.plusSeconds(60),
            epo.plusSeconds(70),
            epo.plusSeconds(700),
            epo.plusSeconds(7000),
            epo.plusSeconds(7001),
            epo.plusSeconds(7002),
            epo.plusSeconds(7003),
            epo.plusSeconds(8000)
        ).iterator();

        var onDemand = new OnDemand(times::next);

        Supplier<Long> nextLong = new AtomicLong()::getAndIncrement;
        var ls = onDemand.<Long>after(Duration.ofSeconds(15)).get(nextLong);

        assertEquals(0L, ls.get()); // init
        assertEquals(0L, ls.get()); // 0
        assertEquals(0L, ls.get()); // 10
        assertEquals(1L, ls.get()); // 20
        onDemand.reset(ls);
        assertEquals(2L, ls.get()); // 30
        assertEquals(2L, ls.get()); // 40
        assertEquals(3L, ls.get()); // 50
        assertEquals(3L, ls.get()); // 60
        assertEquals(4L, ls.get()); // 70
        assertEquals(5L, ls.get()); // 700
        assertEquals(6L, ls.get()); // 7000
        onDemand.reset(ls);
        assertEquals(7L, ls.get()); // 7001
        onDemand.force(ls, 100L); // 7002
        assertEquals(100L, ls.get()); // 7003
        assertEquals(8L, ls.get()); // 8000
    }
}
