package com.github.kjetilv.uplift.fq;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class SimpleFqBatcher<T>
    implements FqBatcher<T> {

    private final FqPuller<T> puller;

    private final int batchSize;

    SimpleFqBatcher(FqPuller<T> puller, int batchSize) {
        this.puller = Objects.requireNonNull(puller, "puller");
        if (batchSize > 0) {
            this.batchSize = batchSize;
        } else {
            throw new IllegalArgumentException("Batch size must be positive: " + batchSize);
        }
    }

    @Override
    public Class<T> type() {
        return puller.type();
    }

    @Override
    public boolean done() {
        return puller.done();
    }

    @Override
    public String name() {
        return puller.name();
    }

    @Override
    public Stream<List<T>> read() {
        return StreamSupport.stream(
            new BatchSpliterator<T>(puller, batchSize),
            false
        );
    }
}
