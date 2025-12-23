package com.github.kjetilv.uplift.fq;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

public sealed interface FqFlows<T> permits AbstractFqFlows {

    static <T> FqFlows<T> create(String name, Fqs<T> fqs) {
        return new SimpleFqFlows<>(name, fqs);
    }

    static <T> FqFlows<T> create(String name, Fqs<T> fqs, int batchSize) {
        return batchSize > 1
            ? new BatchedFqFlows<>(name, fqs, batchSize)
            : new SimpleFqFlows<>(name, fqs);
    }

    default With<T> fromSource(String to) {
        return fromSource().to(to);
    }

    default To<T> fromSource() {
        return from(null);
    }

    default With<T> from(String from, String to) {
        return from(from).to(to);
    }

    To<T> from(String name);

    void feed(Stream<T> items);

    FqFlows<T> onException(Handler<T> handler);

    FqFlows<T> timeout(Duration timeout);

    interface Handler<T> {

        default void failed(Flow<T> flow, T item, Exception e) {
            failed(flow, List.of(item), e);
        }

        void failed(Flow<T> flow, List<T> items, Exception e);
    }

    interface To<T> {

        With<T> to(String name);
    }

    interface With<T> {

        FqFlows<T> with(FqProcessor<T> processor);
    }
}
