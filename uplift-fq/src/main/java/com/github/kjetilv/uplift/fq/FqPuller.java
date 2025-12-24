package com.github.kjetilv.uplift.fq;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface FqPuller<T> extends Fq<T> {

    default Stream<T> stream() {
        return FqStreamer.from(this).read();
    }

    default Stream<List<T>> batches(int batchSize) {
        return FqBatcher.from(this, batchSize).read();
    }

    Optional<T> next();
}
