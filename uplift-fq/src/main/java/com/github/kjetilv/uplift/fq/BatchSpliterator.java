package com.github.kjetilv.uplift.fq;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterators;
import java.util.function.Consumer;

final class BatchSpliterator<T> extends Spliterators.AbstractSpliterator<List<T>> {

    private final FqPuller<T> puller;

    private final int batchSize;

    private List<T> batch;

    BatchSpliterator(FqPuller<T> puller, int batchSize) {
        super(Long.MAX_VALUE, ORDERED);
        this.puller = puller;
        this.batchSize = batchSize;
        if (batchSize > 0) {
            this.batch = new ArrayList<>(this.batchSize);
        } else {
            throw new IllegalArgumentException("batchSize must be positive: " + batchSize);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<T>> action) {
        return puller.next()
            .map(t -> {
                batch.add(t);
                if (batch.size() == this.batchSize) {
                    action.accept(List.copyOf(batch));
                    batch = new ArrayList<>(this.batchSize);
                }
                return true;
            })
            .orElseGet(() -> {
                if (!batch.isEmpty()) {
                    action.accept(List.copyOf(batch));
                }
                return false;
            });

    }
}
