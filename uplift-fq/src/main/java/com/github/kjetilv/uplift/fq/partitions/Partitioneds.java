package com.github.kjetilv.uplift.fq.partitions;

import java.util.List;

public record Partitioneds(List<Partitioned> partitiond) {

    public long total() {
        return partitiond.stream()
            .map(Partitioned::partition)
            .mapToLong(Partition::length)
            .sum();
    }
}
