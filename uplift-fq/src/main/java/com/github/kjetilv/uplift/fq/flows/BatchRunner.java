package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.util.stream.Stream;

final class BatchRunner<T> extends AbstractRunner<T> {

    private final int batchSize;

    BatchRunner(int batchSize, FqFlows.ErrorHandler<T> handler) {
        super(handler);
        if (batchSize > 1) {
            this.batchSize = batchSize;
        } else {
            throw new IllegalArgumentException("batchSize must be >1: " + batchSize);
        }
    }

    @Override
    protected Stream<Entries<T>> entries(Name source, Fqs<T> fqs, Flow<T> flow, FqFlows.ErrorHandler<T> handler) {
        return fqs.batches(source, batchSize)
            .map(items -> {
                var entries = Entries.of(flow.name(), items);
                Entries<T> processed;
                try {
                    processed = flow.processor().process(entries);
                } catch (Exception e) {
                    processed = entries.map(t ->
                        handler.failed(flow, t, e)
                    );
                }
                if (processed.matches(entries)) {
                    return processed;
                }
                throw new IllegalStateException("Expected " + items.size() + " items, got " + processed.size());
            });
    }
}
