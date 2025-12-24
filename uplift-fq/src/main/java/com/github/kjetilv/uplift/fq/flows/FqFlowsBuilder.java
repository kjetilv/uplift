package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.data.Name;

import java.time.Duration;

public interface FqFlowsBuilder<T> {

    default To<T> fromSource() {
        return from(null);
    }

    default With<T> fromSource(Name to) {
        return fromSource().to(to);
    }

    default With<T> from(Name from, Name to) {
        return from(from).to(to);
    }

    To<T> from(Name name);

    FqFlowsBuilder<T> timeout(Duration timeout);

    FqFlowsBuilder<T> onException(ErrorHandler<T> errorHandler);

    FqFlowsBuilder<T> batchSize(int batchSize);

    FqFlows<T> build();

    interface To<T> {

        With<T> to(Name name);
    }

    interface With<T> {

        FqFlowsBuilder<T> with(Processor<T> processor);
    }
}
