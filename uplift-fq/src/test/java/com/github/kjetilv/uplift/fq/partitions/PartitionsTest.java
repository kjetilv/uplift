package com.github.kjetilv.uplift.fq.partitions;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PartitionsTest {

    @Test
    void noPartitions() {
        assertThat(IntStream.range(61, 120 + 10))
            .allSatisfy(biggerThanHalf ->
                assertThat(new Partitioning((long) biggerThanHalf).of(120)).isEmpty());
    }

    @Test
    void simplePartitions() {
        assertThat(IntStream.range(41, 60 + 1))
            .allSatisfy(biggerThanAThird ->
                assertThat(new Partitioning((long) biggerThanAThird).of(120))
                    .hasValueSatisfying(partitions ->
                        assertThat(partitions.partitions()).containsExactly(
                            new Partition(0, 2, 0, 60),
                            new Partition(1, 2, 60, 60)
                        )
                    ));
    }

    @Test
    void evenPartitions() {
        var partitions = new Partitioning(40).of(120);
        assertThat(partitions).hasValueSatisfying(p ->
            assertThat(p.partitions()).containsExactly(
                new Partition(0, 3, 0, 40),
                new Partition(1, 3, 40, 40),
                new Partition(2, 3, 80, 40)
            ));
    }

    @Test
    void overflowPartitions() {
        var partitions = new Partitioning(40).of(122);
        assertThat(partitions).hasValueSatisfying(p ->
            assertThat(p.partitions()).containsExactly(
                new Partition(0, 3, 0, 41),
                new Partition(1, 3, 41, 41),
                new Partition(2, 3, 82, 40)
            ));
    }
}