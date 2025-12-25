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
            .map(item ->
                entry(flow, item))
            .map(entry -> {
                try {
                    return flow.processor().process(entry);
                } catch (Exception e) {
                    return entry.map(t ->
                        handler.failed(flow, t, e));
                }
            });
    }

    private static <T> Entries<T> entry(Flow<T> flow, T item) {
        return Entries.single(flow.to(), item);
    }
}
