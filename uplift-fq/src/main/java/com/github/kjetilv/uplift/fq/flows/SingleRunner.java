package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.util.stream.Stream;

final class SingleRunner<T> extends AbstractRunner<T> {

    SingleRunner(FqFlows.ErrorHandler<T> handler) {
        super(handler);
    }

    @Override
    protected Stream<Entries<T>> entries(
        Name source,
        Fqs<T> fqs,
        Flow<T> flow,
        FqFlows.ErrorHandler<T> handler
    ) {
        return fqs.streamer(source)
            .read()
            .map(item -> {
                var entry = Entries.single(flow.to(), item);
                try {
                    return flow.processor().process(entry);
                } catch (Exception e) {
                    return entry.map(t -> handler.failed(flow, t, e));
                }
            });
    }
}
