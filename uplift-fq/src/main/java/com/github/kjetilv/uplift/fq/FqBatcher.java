package com.github.kjetilv.uplift.fq;

import java.util.List;
import java.util.stream.Stream;

public interface FqBatcher<T> extends Fq<T> {

    static <T> FqBatcher<T> from(FqPuller<T> puller, int batchSize) {
        return new FqBatcherImpl<>(puller, batchSize);
    }

    Stream<List<T>> read();
}
