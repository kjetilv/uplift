package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.util.stream.Stream;

final class SequentialBatchRunner<T> extends AbstractSequentialFlowRunner<T> {

    private final int batchSize;

    SequentialBatchRunner(int batchSize, FqFlows.ErrorHandler<T> handler) {
        super(handler);
        if (batchSize < 2) {
            throw new IllegalArgumentException("batchSize must be 2+: " + batchSize);
        }
        this.batchSize = batchSize;
    }

    @Override
    protected Stream<Entries<T>> entries(
        Fqs<T> fqs,
        Flow<T> flow,
        FqFlows.ErrorHandler<T> handler
    ) {
        return fqs.reader(flow.from()).batches(batchSize)
            .map(items ->
                Entries.of(flow.name(), items))
            .map(entries -> {
                try {
                    return flow.processor().process(entries);
                } catch (Exception e) {
                    return entries.map(t ->
                        handler.failed(flow, t, e));
                }
            });
    }
}
