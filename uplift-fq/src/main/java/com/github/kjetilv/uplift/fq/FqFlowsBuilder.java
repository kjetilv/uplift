package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.data.Name;

import java.time.Duration;
import java.util.Objects;

public final class FqFlowsBuilder<T> {

    private final Name name;

    private final Fqs<T> fqs;

    private int batchSize;

    private Duration timeout;

    private ErrorHandler<T> errorHandler;

    public FqFlowsBuilder(Name name, Fqs<T> fqs) {
        this.name = Objects.requireNonNull(name, "name");
        this.fqs = Objects.requireNonNull(fqs, "fqs");
    }

    public FqFlowsBuilder<T> batchSize(int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be at least 1");
        }
        this.batchSize = batchSize;
        return this;
    }

    public FqFlowsBuilder<T> timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public FqFlowsBuilder<T> onException(ErrorHandler<T> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public FqFlows<T> build() {
        return new DefaultFqFlows<>(
            name,
            fqs,
            timeout,
            batchSize > 1
                ? new BatchRunner<>(batchSize, errorHandler)
                : new SingleRunner<>(errorHandler)
        );
    }
}
