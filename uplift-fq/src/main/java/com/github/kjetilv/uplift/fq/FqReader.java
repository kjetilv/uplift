package com.github.kjetilv.uplift.fq;

import module java.base;

import static java.util.stream.Gatherers.windowFixed;

public interface FqReader<T> {

    default Stream<List<T>> batches(int batchSize) {
        return stream().gather(windowFixed(batchSize));
    }

    default Stream<T> stream() {
        return Stream.generate(this::next).takeWhile(Objects::nonNull);
    }

    T next();
}
