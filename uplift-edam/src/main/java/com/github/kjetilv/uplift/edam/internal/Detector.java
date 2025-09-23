package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module uplift.edam;
import module uplift.hash;

record Detector(int depth) {

    <K extends HashKind<K>> List<HashPattern<K>> patterns(
        LongFunction<? extends Supplier<Hash<K>>> get,
        long count
    ) {
        return LongStream.range(0, count).mapToObj(i ->
                patterns(get, i, 0, count))
            .flatMap(Function.identity())
            .distinct()
            .map(HashPattern::cyclicSubPattern)
            .distinct()
            .sorted()
            .toList();
    }

    private <K extends HashKind<K>> Stream<HashPattern<K>> patterns(
        LongFunction<? extends Supplier<Hash<K>>> get,
        long index,
        int length,
        long count
    ) {
        return index == count || depth > 0 && length >= depth
            ? Stream.empty()
            : expand(get, index, length, count, single(get, index));
    }

    private <K extends HashKind<K>> Stream<HashPattern<K>> expand(
        LongFunction<? extends Supplier<Hash<K>>> get,
        long index,
        int length,
        long count,
        HashPattern<K> hashPattern
    ) {
        return Stream.concat(
            Stream.of(hashPattern),
            remainingIndices(index, count)
                .mapToObj(i ->
                    patterns(get, i, length + 1, count))
                .flatMap(Function.identity())
                .distinct()
                .map(hashPattern::with)
        );
    }

    private static <K extends HashKind<K>> HashPattern<K> single(
        LongFunction<? extends Supplier<Hash<K>>> get,
        long index
    ) {
        return new HashPattern<>(get.apply(index).get());
    }

    private static LongStream remainingIndices(long index, long size) {
        return LongStream.range(index + 1, size);
    }
}
