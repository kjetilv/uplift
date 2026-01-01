package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class DefaultBuilder<T> implements FqFlows.Builder<T> {

    private final Name name;

    private final Fqs<T> fqs;

    private Integer batchSize;

    private Duration timeout;

    private FqFlows.ErrorHandler<T> handler;

    private final List<Flow<T>> flows = new ArrayList<>();

    DefaultBuilder(Name name, Fqs<T> fqs) {
        this.name = requireNonNull(name, "name");
        this.fqs = requireNonNull(fqs, "fqs");
    }

    @Override
    public FqFlows.Builder<T> timeout(Duration timeout) {
        if (this.timeout != null) {
            throw new IllegalStateException("Timeout already set");
        }
        this.timeout = requireNonNull(timeout, "timeout");
        return this;
    }

    @Override
    public FqFlows.Builder<T> onException(FqFlows.ErrorHandler<T> errorHandler) {
        if (handler != null) {
            throw new IllegalStateException("Error handler already set");
        }
        this.handler = requireNonNull(errorHandler, "errorHandler");
        return this;
    }

    @Override
    public FqFlows.Builder<T> batchSize(int batchSize) {
        if (this.batchSize != null) {
            throw new IllegalStateException("Batch size already set");
        }
        if (batchSize > 1) {
            this.batchSize = batchSize;
            return this;
        }
        throw new IllegalArgumentException("batchSize must be at least 1");
    }

    @Override
    public With<T> then(Name to) {
        return from(flows.isEmpty() ? name : flows.getLast().to(), to);
    }

    public FqFlows.Builder.To<T> from(Name from) {
        return to ->
            process -> {
                var flow = new Flow<>(from, to, process);
                flows.add(validated(flow));
                return this;
            };
    }

    @Override
    public FqFlows<T> build() {
        Flows.validateAll(flows);

        var handler = this.handler == null
            ? fail()
            : this.handler;

        var runner = batchSize == null
            ? new SequentialSingleRunner<>(handler)
            : new SequentialBatchRunner<>(batchSize, handler);

        return new DefaultFqFlows<>(name, fqs, timeout, runner, flows);
    }

    private Flow<T> validated(Flow<T> flow) {
        var combined = Stream.concat(this.flows.stream(), Stream.of(flow))
            .toList();
        Flows.validate(combined);
        return flow;
    }

    private FqFlows.ErrorHandler<T> fail() {
        return (flow, item, error) -> {
            throw new RuntimeException("Failed to process " + item + " " + flow.description(), error);
        };
    }
}
