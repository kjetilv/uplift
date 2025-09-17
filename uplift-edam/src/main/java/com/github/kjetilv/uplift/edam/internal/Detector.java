package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.patterns.Pattern;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;

record Detector(int depth) {

    <K extends HashKind<K>> List<Pattern<K>> patterns(
        LongFunction<? extends Supplier<Hash<K>>> get,
        long count
    ) {
        return LongStream.range(0, count).mapToObj(i ->
                patterns(get, i, 0, count))
            .flatMap(Function.identity())
            .distinct()
            .map(Pattern::cyclicSubPattern)
            .distinct()
            .sorted()
            .toList();
    }

    private <K extends HashKind<K>> Stream<Pattern<K>> patterns(
        LongFunction<? extends Supplier<Hash<K>>> get,
        long index,
        int length,
        long count
    ) {
        return index == count || depth > 0 && length >= depth
            ? Stream.empty()
            : expand(get, index, length, count, single(get, index));
    }

    private <K extends HashKind<K>> Stream<Pattern<K>> expand(
        LongFunction<? extends Supplier<Hash<K>>> get,
        long index,
        int length,
        long count,
        Pattern<K> pattern
    ) {
        return Stream.concat(
            Stream.of(pattern),
            remainingIndices(index, count)
                .mapToObj(i ->
                    patterns(get, i, length + 1, count))
                .flatMap(Function.identity())
                .distinct()
                .map(pattern::with)
        );
    }

    private static <K extends HashKind<K>> Pattern<K> single(
        LongFunction<? extends Supplier<Hash<K>>> get,
        long index
    ) {
        return new Pattern<>(get.apply(index).get());
    }

    private static LongStream remainingIndices(long index, long size) {
        return LongStream.range(index + 1, size);
    }
}
