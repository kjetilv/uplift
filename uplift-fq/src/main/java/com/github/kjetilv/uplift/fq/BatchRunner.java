package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.data.Name;

import java.util.List;
import java.util.stream.Stream;

final class BatchRunner<T> implements DefaultFqFlows.Runner<T> {

    private final int batchSize;

    private final ErrorHandler<T> handler;

    BatchRunner(int batchSize, ErrorHandler<T> handler) {
        this.batchSize = batchSize;
        this.handler = handler;
    }

    @Override
    public void run(Name source, Fqs<T> fqs, Flow<T> flow) {
        try (var writer = fqs.writer(flow.to())) {
            fqs.batches(flow.fromOr(source), batchSize)
                .flatMap(items -> {
                    List<T> processed;
                    try {
                        processed = flow.processor().process(items);
                    } catch (Exception e) {
                        handler.failed(flow, items, e);
                        return Stream.empty();
                    }
                    if (processed.size() != items.size()) {
                        throw new IllegalStateException("Expected " + items.size() + " items, got " + processed.size());
                    }
                    return Stream.of(processed);
                })
                .forEach(writer::write);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute " + flow, e);
        }
    }
}
