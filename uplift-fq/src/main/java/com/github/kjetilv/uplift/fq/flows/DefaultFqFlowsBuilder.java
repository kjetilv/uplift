package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;
import com.github.kjetilv.uplift.fq.data.Name;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class DefaultFqFlowsBuilder<T> implements FqFlowsBuilder<T> {

    private final Name name;

    private final Fqs<T> fqs;

    private final List<Flow<T>> flows = new ArrayList<>();

    private Integer batchSize;

    private Duration timeout;

    private ErrorHandler<T> handler;

    DefaultFqFlowsBuilder(Name name, Fqs<T> fqs) {
        this.name = Objects.requireNonNull(name, "name");
        this.fqs = Objects.requireNonNull(fqs, "fqs");
    }

    @Override
    public FqFlowsBuilder<T> timeout(Duration timeout) {
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        return this;
    }

    @Override
    public FqFlowsBuilder<T> onException(ErrorHandler<T> errorHandler) {
        this.handler = Objects.requireNonNull(errorHandler, "errorHandler");
        return this;
    }

    @Override
    public FqFlowsBuilder<T> batchSize(int batchSize) {
        if (batchSize > 1) {
            this.batchSize = batchSize;
            return this;
        }
        throw new IllegalArgumentException("batchSize must be at least 1");
    }

    public FqFlowsBuilder.To<T> from(Name from) {
        return to ->
            process -> {
                Flow<T> flow = new Flow<>(from, to, process);
                flows.add(validated(flow));
                return this;
            };
    }

    @Override
    public FqFlows<T> build() {
        var handler = this.handler != null
            ? this.handler
            : fail();
        var runner = batchSize == null
            ? new SingleRunner<>(handler)
            : new BatchRunner<>(batchSize, handler);
        return new DefaultFqFlows<>(
            name,
            fqs,
            timeout,
            runner,
            flows
        );
    }

    private Flow<T> validated(Flow<T> flow) {
        var flows = Stream.concat(this.flows.stream(), Stream.of(flow))
            .toList();
        validate(flows);
        return flow;
    }

    private ErrorHandler<T> fail() {
        return (flow, items, error) -> {
            throw new RuntimeException(
                "Failed to process " + items.size() + " items " + flow.description() + ":  " + items,
                error
            );
        };
    }

    private static <T> void validate(List<Flow<T>> flows) {
        var sources = flows.stream()
            .filter(Flow::isFromSource)
            .toList();
        if (sources.isEmpty()) {
            throw new IllegalStateException("No source intake defined: " + print(flows));
        }
        var downstream = flows.stream()
            .filter(flow ->
                !flow.isFromSource())
            .toList();
        var tos = flows.stream()
            .collect(Collectors.groupingBy(Flow::to));
        var missingInputs = downstream.stream()
            .filter(flow -> !tos.containsKey(flow.to()))
            .toList();
        if (!missingInputs.isEmpty()) {
            throw new IllegalStateException("Missing inputs to flows: " + print(missingInputs));
        }
    }

    private static <T> String print(List<Flow<T>> flows) {
        return flows.stream()
            .map(Flow::description)
            .collect(Collectors.joining(" "));
    }
}
