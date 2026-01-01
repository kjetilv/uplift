package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

abstract sealed class AbstractSequentialFlowRunner<T> implements FlowRunner<T>
    permits SequentialBatchRunner, SequentialSingleRunner {

    private final FqFlows.ErrorHandler<T> handler;

    AbstractSequentialFlowRunner(FqFlows.ErrorHandler<T> handler) {
        this.handler = requireNonNull(handler, "handler");
    }

    @Override
    public final void run(Fqs<T> fqs, Flow<T> flow) {
        try (var writer = fqs.writer(flow.to())) {
            entries(fqs, flow, handler)
                .forEach(entries -> {
                    writer.write(entries.items());
                });
        }
    }

    protected abstract Stream<Entries<T>> entries(
        Fqs<T> fqs,
        Flow<T> flow,
        FqFlows.ErrorHandler<T> handler
    );
}
