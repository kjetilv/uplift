package com.github.kjetilv.uplift.fq.partitions;

import module java.base;

public record Partitioneds(List<Partitioned> partitiond) {

    public long total() {
        return partitiond.stream()
            .map(Partitioned::partition)
            .mapToLong(Partition::length)
            .sum();
    }
}
