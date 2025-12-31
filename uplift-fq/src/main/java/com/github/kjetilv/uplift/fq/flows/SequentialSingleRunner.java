package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.util.stream.Stream;

final class SequentialSingleRunner<T> extends AbstractSequentialFlowRunner<T> {

    SequentialSingleRunner(FqFlows.ErrorHandler<T> handler) {
        super(handler);
    }

    @Override
    protected Stream<Entries<T>> entries(
        Fqs<T> fqs,
        Flow<T> flow,
        FqFlows.ErrorHandler<T> handler
    ) {
        return fqs.reader(flow.from()).stream()
            .map(item ->
                Entries.single(flow.to(), item))
            .map(entry -> {
                try {
                    return flow.processor().process(entry);
                } catch (Exception e) {
                    return entry.map(t ->
                        handler.failed(flow, t, e));
                }
            });
    }

}
