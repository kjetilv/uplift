package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.util.Objects;
import java.util.stream.Stream;

abstract class AbstractRunner<T> implements DefaultFqFlows.Runner<T> {

    private final FqFlows.ErrorHandler<T> handler;

    AbstractRunner(FqFlows.ErrorHandler<T> handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public final void run(Name source, Fqs<T> fqs, Flow<T> flow) {
        try (var writer = fqs.writer(flow.to())) {
            entries(source, fqs, flow, handler)
                .forEach(entries -> {
                    writer.write(entries.items());
                });
        }
    }

    protected abstract Stream<Entries<T>> entries(
        Name source,
        Fqs<T> fqs,
        Flow<T> flow,
        FqFlows.ErrorHandler<T> handler
    );
}
