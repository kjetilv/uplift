package com.github.kjetilv.uplift.fq;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

final class SingleFqFlows<T>
    extends AbstractFqFlows<T> {

    SingleFqFlows(String name, Fqs<T> fqs) {
        this(name, fqs, null, null, null);
    }

    SingleFqFlows(
        String name,
        Fqs<T> fqs,
        List<Flow<T>> flows,
        Duration timeout,
        ErrorHandler<T> handler
    ) {
        super(name, fqs, 0, timeout, flows, handler);
    }

    @Override
    public FqFlows<T> onException(ErrorHandler<T> handler) {
        return new SingleFqFlows<>(name, fqs, flows, timeout, handler);
    }

    @Override
    public FqFlows<T> timeout(Duration timeout) {
        return new SingleFqFlows<>(name, fqs, flows, timeout, handler);
    }

    @Override
    protected void run(String source, Flow<T> flow, FqWriter<T> writer) {
        fqs.streamer(source).read().flatMap(item -> {
                try {
                    return Stream.of(flow.processor().process(item));
                } catch (Exception e) {
                    handler.failed(flow, item, e);
                    return Stream.empty();
                }
            })
            .forEach(writer::write);
    }

    @Override
    protected FqFlows<T> with(List<Flow<T>> flows) {
        return new SingleFqFlows<>(name, fqs, flows, timeout, handler);
    }
}
