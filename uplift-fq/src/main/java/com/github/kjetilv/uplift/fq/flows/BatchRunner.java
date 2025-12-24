package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;
import com.github.kjetilv.uplift.fq.data.Name;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

final class BatchRunner<T> implements DefaultFqFlows.Runner<T> {

    private final int batchSize;

    private final ErrorHandler<T> handler;

    BatchRunner(int batchSize, ErrorHandler<T> handler) {
        if (batchSize > 1) {
            this.batchSize = batchSize;
        } else {
            throw new IllegalArgumentException("batchSize must be >1: " + batchSize);
        }
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public void run(Name source, Fqs<T> fqs, Flow<T> flow) {
        try (var writer = fqs.writer(flow.to())) {
            var name = flow.fromOr(source);
            fqs.batches(name, batchSize)
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
        }
    }
}
