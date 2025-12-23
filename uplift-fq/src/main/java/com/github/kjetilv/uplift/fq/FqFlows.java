package com.github.kjetilv.uplift.fq;

import java.time.Duration;
import java.util.stream.Stream;

public sealed interface FqFlows<T> permits AbstractFqFlows {

    static <T> FqFlows<T> create(String name, Fqs<T> fqs) {
        return new SingleFqFlows<>(name, fqs);
    }

    static <T> FqFlows<T> create(String name, Fqs<T> fqs, int batchSize) {
        return batchSize > 1
            ? new BatchedFqFlows<>(name, fqs, batchSize)
            : new SingleFqFlows<>(name, fqs);
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

    FqFlows<T> onException(ErrorHandler<T> handler);

    FqFlows<T> timeout(Duration timeout);

    interface To<T> {

        With<T> to(String name);
    }

    interface With<T> {

        FqFlows<T> with(FqProcessor<T> processor);
    }
}
