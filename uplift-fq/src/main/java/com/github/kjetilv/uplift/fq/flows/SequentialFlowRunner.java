package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.FqReader;
import com.github.kjetilv.uplift.fq.Fqs;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class SequentialFlowRunner<T> implements FlowRunner<T> {

    private final FqFlows.ErrorHandler<T> handler;

    private final EntryStreamer<T> entryStreamer;

    SequentialFlowRunner(FqFlows.ErrorHandler<T> handler, EntryStreamer<T> entryStreamer) {
        this.handler = requireNonNull(handler, "handler");
        this.entryStreamer = requireNonNull(entryStreamer, "runner");
    }

    @Override
    public void run(Fqs<T> fqs, Flow<T> flow) {
        try (var writer = fqs.writer(flow.to())) {
            var reader = fqs.reader(flow.from());
            entryStreamer.entries(flow, reader)
                .map(entries ->
                    process(flow, entries))
                .forEach(entries ->
                    writer.write(entries.items()));
        }
    }

    private Entries<T> process(
        Flow<T> flow,
        Entries<T> entries
    ) {
        try {
            return flow.process(entries);
        } catch (Exception e) {
            return entries.map(t ->
                handler.failed(flow, t, e)
            );
        }
    }

    interface EntryStreamer<T> {

        Stream<Entries<T>> entries(Flow<T> flow, FqReader<T> reader);
    }
}
