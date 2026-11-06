package com.github.kjetilv.uplift.synchttp.read;

import module java.base;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SegmentsTest {

    @Test
    void concurrentAcquireNeverSharesASegment() {
        var segments = new Segments(Arena.ofAuto());
        var held = ConcurrentHashMap.<Integer>newKeySet();
        var collisions = new LongAdder();
        try (var executor = Executors.newFixedThreadPool(8)) {
            IntStream.range(0, 8).forEach(_ ->
                executor.submit(() ->
                    IntStream.range(0, 100_000).forEach(_ -> {
                        var pooled = segments.acquire();
                        if (held.add(pooled.index())) {
                            held.remove(pooled.index());
                        } else {
                            collisions.increment();
                        }
                        segments.release(pooled);
                    })));
        }
        assertThat(collisions.sum()).isZero();
    }

    @Test
    void exhaustedPoolAllocates() {
        var segments = new Segments(Arena.ofAuto(), 1024, 2048);
        var first = segments.acquire();
        var second = segments.acquire();
        assertThat(first.index()).isNotEqualTo(second.index());
        var extra = segments.acquire();
        assertThat(extra.segment().byteSize()).isEqualTo(1024);
        segments.release(extra);
        segments.release(first);
        assertThat(segments.acquire().index()).isEqualTo(first.index());
        assertThat(segments.acquire().segment().byteSize()).isEqualTo(1024);
    }
}
