package com.github.kjetilv.uplift.fq;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

final class BatchedFqFlows<T>
    extends AbstractFqFlows<T> {

    BatchedFqFlows(String name, Fqs<T> fqs, int batchSize) {
        this(name, fqs, null, batchSize, null, null);
    }

    BatchedFqFlows(
        String name,
        Fqs<T> fqs,
        List<Flow<T>> flows,
        int batchSize,
        Duration timeout,
        ErrorHandler<T> handler
    ) {
        super(name, fqs, batchSize, timeout, flows, handler);
    }

    @Override
    public FqFlows<T> onException(ErrorHandler<T> handler) {
        return new BatchedFqFlows<>(name, fqs, flows, batchSize, timeout, handler);
    }

    @Override
    public FqFlows<T> timeout(Duration timeout) {
        return new BatchedFqFlows<>(name, fqs, flows, batchSize, timeout, handler);
    }

    @Override
    protected void run(String source, Flow<T> flow, FqWriter<T> writer) {
        fqs.batches(source, batchSize).flatMap(items -> {
                try {
                    return Stream.of(flow.processor().process(items));
                } catch (Exception e) {
                    handler.failed(flow, items, e);
                    return Stream.empty();
                }
            })
            .forEach(writer::write);
    }

    @Override
    protected FqFlows<T> with(List<Flow<T>> flows) {
        return new BatchedFqFlows<>(name, fqs, flows, batchSize, timeout, handler);
    }
}
