package com.github.kjetilv.uplift.fq.partitions;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Partitions(List<Partition> partitions) {

    public static Optional<Partitions> of(long total, long minSize) {
        if (total < minSize * 2) {
            return Optional.empty();
        }
        long trailing = total % minSize;
        if (trailing == 0) {
            return Optional.of(check(total, evenPartitions(total, minSize)));
        }
        var partitions = prettyEvenPartitions(total, minSize);
        return Optional.of(check(total, partitions));
    }

    public long total() {
        return partitions.stream().mapToLong(Partition::length).sum();
    }

    private static Partitions evenPartitions(long total, long minSize) {
        return new Partitions(
            IntStream.range(0, Math.toIntExact(total / minSize))
                .mapToObj(no ->
                    new Partition(no, no * minSize, minSize))
                .toList()
        );
    }

    private static Partitions prettyEvenPartitions(long total, long minSize) {
        int count = Math.toIntExact(total / minSize);
        int tail = Math.toIntExact(total % count);
        long length = total / count;
        var headLength = tail * (length + 1);

        var headLengths = IntStream.range(0, tail)
            .mapToObj(i ->
                new Partition(i, i * (length + 1), length + 1))
            .toList();
        var tailLengths = IntStream.range(0, count - tail)
            .mapToObj(i ->
                new Partition(i + tail, headLength + i * length, length))
            .toList();
        return new Partitions(Stream.of(headLengths, tailLengths)
            .flatMap(List::stream)
            .toList());
    }

    private static Partitions check(long total, Partitions partitions) {
        assert partitions.total() == total
            : "Expected same count: " + partitions.total() + " != " + total;
        return partitions;
    }
}
