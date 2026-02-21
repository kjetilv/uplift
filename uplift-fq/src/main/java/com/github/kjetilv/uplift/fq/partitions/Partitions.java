package com.github.kjetilv.uplift.fq.partitions;

import module java.base;

public record Partitions(List<Partition> partitions) {

    public Partitions {
        if (partitions == null || partitions.isEmpty()) {
            throw new IllegalArgumentException("No partitions defined");
        }
        if (!partitions.getFirst().first()) {
            throw new IllegalStateException("Invalid partitioning: first partition must be first(): " + partitions);
        }
        if (!partitions.getLast().last()) {
            throw new IllegalStateException("Invalid partitioning: last partition must be last(): " + partitions);
        }
    }

    public long total() {
        return partitions.stream()
            .mapToLong(Partition::length)
            .sum();
    }
}
