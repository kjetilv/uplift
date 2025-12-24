package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.data.Name;

import java.util.stream.Stream;

public sealed interface FqFlows<T> permits DefaultFqFlows {

    static <T> FqFlowsBuilder<T> builder(Name name, Fqs<T> fqs) {
        return new FqFlowsBuilder<>(name, fqs);
    }

    default With<T> fromSource(Name to) {
        return fromSource().to(to);
    }

    default To<T> fromSource() {
        return from(null);
    }

    default With<T> from(Name from, Name to) {
        return from(from).to(to);
    }

    To<T> from(Name name);

    void feed(Stream<T> items);

    interface To<T> {

        With<T> to(Name name);
    }

    interface With<T> {

        FqFlows<T> with(FqProcessor<T> processor);
    }
}
