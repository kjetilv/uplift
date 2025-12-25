package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.util.List;
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
    protected Stream<Entries<T>> entries(
        Name source,
        Fqs<T> fqs,
        Flow<T> flow,
        FqFlows.ErrorHandler<T> handler
    ) {
        return fqs.batches(source, batchSize)
            .map(items ->
                entries(flow, items))
            .map(entries ->
                process(flow, entries, handler)
            );
    }

    private static <T> Entries<T> entries(Flow<T> flow, List<T> items) {
        return Entries.of(flow.name(), items);
    }

    private static <T> Entries<T> process(
        Flow<T> flow,
        Entries<T> entries,
        FqFlows.ErrorHandler<T> handler
    ) {
        try {
            return flow.processor().process(entries);
        } catch (Exception e) {
            return entries.map(t ->
                handler.failed(flow, t, e)
            );
        }
    }
}
