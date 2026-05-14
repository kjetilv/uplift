package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.patterns.HashPattern;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

record Detector(int depth) {

    <H extends HashKind<H>> List<HashPattern<H>> patterns(
        LongFunction<? extends Supplier<Hash<H>>> get,
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

    private <H extends HashKind<H>> Stream<HashPattern<H>> patterns(
        LongFunction<? extends Supplier<Hash<H>>> get,
        long index,
        int length,
        long count
    ) {
        return index == count || depth > 0 && length >= depth
            ? Stream.empty()
            : expand(get, index, length, count, single(get, index));
    }

    private <H extends HashKind<H>> Stream<HashPattern<H>> expand(
        LongFunction<? extends Supplier<Hash<H>>> get,
        long index,
        int length,
        long count,
        HashPattern<H> hashPattern
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

    private static <H extends HashKind<H>> HashPattern<H> single(
        LongFunction<? extends Supplier<Hash<H>>> get,
        long index
    ) {
        return new HashPattern<>(get.apply(index).get());
    }

    private static LongStream remainingIndices(long index, long size) {
        return LongStream.range(index + 1, size);
    }
}
