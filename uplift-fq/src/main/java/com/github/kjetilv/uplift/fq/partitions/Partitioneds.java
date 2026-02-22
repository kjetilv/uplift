package com.github.kjetilv.uplift.fq.partitions;

import module java.base;

public record Partitioneds(List<Partitioned> partitioneds) {

    public long total() {
        return partitioneds.stream()
            .map(Partitioned::partition)
            .mapToLong(Partition::length)
            .sum();
    }
}
