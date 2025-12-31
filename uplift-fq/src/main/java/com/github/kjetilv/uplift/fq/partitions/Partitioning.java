package com.github.kjetilv.uplift.fq.partitions;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Partitioning(long minSize) {

    public Optional<Partitions> of(long total) {
        if (total < minSize * 2) {
            return Optional.empty();
        }
        var trailing = total % minSize;
        if (trailing == 0) {
            return Optional.of(check(total, evenPartitions(total, minSize)));
        }
        var partitions = mostlyEvenPartitions(total, minSize);
        return Optional.of(check(total, partitions));
    }

    private static Partitions check(long total, Partitions partitions) {
        assert partitions.total() == total
            : "Expected same count: " + partitions.total() + " != " + total;
        return partitions;
    }

    private static Partitions evenPartitions(long total, long minSize) {
        var count = Math.toIntExact(total / minSize);
        return new Partitions(
            IntStream.range(0, count)
                .mapToObj(no ->
                    new Partition(no, count, no * minSize, minSize))
                .toList()
        );
    }

    private static Partitions mostlyEvenPartitions(long total, long minSize) {
        var count = Math.toIntExact(total / minSize);
        var tail = Math.toIntExact(total % count);
        var length = total / count;
        var headLength = tail * (length + 1);

        var headLengths = IntStream.range(0, tail)
            .mapToObj(i ->
                new Partition(i, count, i * (length + 1), length + 1))
            .toList();
        var tailLengths = IntStream.range(0, count - tail)
            .mapToObj(i ->
                new Partition(i + tail, count, headLength + i * length, length))
            .toList();
        return new Partitions(Stream.of(headLengths, tailLengths)
            .flatMap(List::stream)
            .toList());
    }
}
