package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;
import com.github.kjetilv.uplift.fq.data.Name;

import java.util.Objects;
import java.util.stream.Stream;

final class SingleRunner<T> implements DefaultFqFlows.Runner<T> {

    private final ErrorHandler<T> handler;

    SingleRunner(ErrorHandler<T> handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public void run(Name source, Fqs<T> fqs, Flow<T> flow) {
        try (var writer = fqs.writer(flow.to())) {
            fqs.streamer(flow.fromOr(source))
                .read()
                .flatMap(item -> {
                    try {
                        return Stream.of(flow.processor().process(item));
                    } catch (Exception e) {
                        handler.failed(flow, item, e);
                        return Stream.empty();
                    }
                })
                .forEach(writer::write);
        }
    }
}
