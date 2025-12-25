package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class DefaultBuilder<T> implements FqFlows.Builder<T> {

    private final Name name;

    private final Fqs<T> fqs;

    private final List<Flow<T>> flows = new ArrayList<>();

    private Integer batchSize;

    private Duration timeout;

    private FqFlows.ErrorHandler<T> handler;

    DefaultBuilder(Name name, Fqs<T> fqs) {
        this.name = Objects.requireNonNull(name, "name");
        this.fqs = Objects.requireNonNull(fqs, "fqs");
    }

    @Override
    public FqFlows.Builder<T> timeout(Duration timeout) {
        if (this.timeout != null) {
            throw new IllegalStateException("Timeout already set");
        }
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        return this;
    }

    @Override
    public FqFlows.Builder<T> onException(FqFlows.ErrorHandler<T> errorHandler) {
        if (handler != null) {
            throw new IllegalStateException("Error handler already set");
        }
        this.handler = Objects.requireNonNull(errorHandler, "errorHandler");
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

    public FqFlows.Builder.To<T> from(Name from) {
        return to ->
            process -> {
                var flow = new Flow<T>(from, to, process);
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
        validate(Stream.concat(this.flows.stream(), Stream.of(flow))
            .toList());
        return flow;
    }

    private FqFlows.ErrorHandler<T> fail() {
        return (flow, item, error) -> {
            throw new RuntimeException("Failed to process " + item + " " + flow.description(), error);
        };
    }

    private static <T> void validate(List<Flow<T>> flows) {
        var sources = flows.stream()
            .filter(Flow::isFromSource)
            .toList();
        if (sources.isEmpty()) {
            throw new IllegalStateException("No source intake defined: " + print(flows));
        }
        var groups = flows.stream()
            .collect(Collectors.groupingBy(Flow::description));
        var dupes = groups.entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));
        if (!dupes.isEmpty()) {
            throw new IllegalStateException(
                "Duplicate flow descriptions: " + String.join(", ", dupes.keySet())
            );
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
