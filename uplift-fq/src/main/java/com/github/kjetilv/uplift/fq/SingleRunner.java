package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.data.Name;

import java.util.stream.Stream;

final class SingleRunner<T> implements DefaultFqFlows.Runner<T> {

    private final ErrorHandler<T> handler;

    SingleRunner(ErrorHandler<T> errorHandler) {
        this.handler = errorHandler;
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
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute " + flow, e);
        }
    }
}
