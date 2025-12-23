package com.github.kjetilv.uplift.fq;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ProtectedField")
abstract sealed class AbstractFqFlows<T>
    implements FqFlows<T>
    permits BatchedFqFlows, SimpleFqFlows {

    protected final String name;

    protected final Fqs<T> fqs;

    protected final int batchSize;

    protected final Duration timeout;

    protected final List<Flow<T>> flows;

    protected final Handler<T> handler;

    AbstractFqFlows(
        String name,
        Fqs<T> fqs,
        int batchSize,
        Duration timeout,
        List<Flow<T>> flows,
        Handler<T> handler
    ) {
        this.name = Objects.requireNonNull(name, "name");
        if (this.name.isBlank()) {
            throw new IllegalArgumentException("Could not create flows with empty name");
        }
        this.fqs = Objects.requireNonNull(fqs, "fqs");
        this.batchSize = Math.max(batchSize, 1);
        this.timeout = timeout;
        this.flows = flows == null || flows.isEmpty()
            ? List.of()
            : List.copyOf(flows);
        this.handler = handler;
    }

    @Override
    public To<T> from(String from) {
        return to ->
            process ->
                this.withFlow(new Flow<>(from, to, process));
    }

    @Override
    public void feed(Stream<T> items) {
        validateFlows(flows);
        try (var writer = fqs.writer(this.name)) {
            items.forEach(writer::write);
        }
        try (
            var scope =
                StructuredTaskScope.open(
                    StructuredTaskScope.Joiner.allSuccessfulOrThrow(),
                    this::configure
                )
        ) {
            try {
                flows.forEach(flow ->
                    scope.fork(() -> {
                        try (var writer = fqs.writer(flow.to())) {
                            run(flow.fromOr(this.name), flow, writer);
                        }
                    }));
                var count = scope.join().count();
                assert count == flows.size() : "Expected " + flows.size() + ", got " + count;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Flow execution interrupted", e);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to execute flow", e);
            }
        }
    }

    protected abstract void run(String source, Flow<T> flow, FqWriter<T> writer);

    protected abstract FqFlows<T> with(List<Flow<T>> flows);

    private StructuredTaskScope.Configuration configure(StructuredTaskScope.Configuration cf) {
        var named = cf
            .withName(name)
            .withThreadFactory(threadFactory(name));
        return timeout == null ? named
            : named.withTimeout(Duration.ofMinutes(5));
    }

    private FqFlows<T> withFlow(Flow<T> flow) {
        return with(newList(flow));
    }

    private List<Flow<T>> newList(Flow<T> flow) {
        return Stream.concat(flows.stream(), Stream.of(flow))
            .toList();
    }

    private static <T> String print(List<Flow<T>> flows) {
        return flows.stream()
            .map(Flow::description)
            .collect(Collectors.joining(" "));
    }

    private static <T> void validateFlows(List<Flow<T>> flows) {
        if (flows.isEmpty()) {
            throw new IllegalStateException("No flows defined");
        }
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

    private static ThreadFactory threadFactory(String namePrefix) {
        LongAdder i = new LongAdder();
        return runnable -> {
            try {
                return Thread.ofVirtual()
                    .name(namePrefix + "-" + i.longValue())
                    .unstarted(runnable);
            } finally {
                i.increment();
            }
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ": " + batchSize + "]";
    }
}
