package com.github.kjetilv.uplift.fq.partitions;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PartitionsTest {

    @Test
    void noPartitions() {
        assertThat(IntStream.range(61, 120 + 1))
            .allSatisfy(i ->
                assertThat(Partitions.of(120, i)).isEmpty());
    }

    @Test
    void simplePartitions() {
        assertThat(IntStream.range(41, 60 + 1))
            .allSatisfy(i ->
                assertThat(Partitions.of(120, i))
                    .hasValueSatisfying(partitions ->
                        assertThat(partitions.partitions()).containsExactly(
                            new Partition(0, 0, 60),
                            new Partition(1, 60, 60)
                        )
                    ));
    }

    @Test
    void overflowPartitions() {
        var partitions = Partitions.of(122, 40);
        assertThat(partitions).hasValueSatisfying(p ->
            assertThat(p.partitions()).containsExactly(
                new Partition(0, 0, 41),
                new Partition(1, 41, 41),
                new Partition(2, 82, 40)
            ));
    }
}