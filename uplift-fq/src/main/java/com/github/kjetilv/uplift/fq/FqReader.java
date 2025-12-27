package com.github.kjetilv.uplift.fq;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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
