package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.data.Name;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Configuration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class DefaultFqFlows<T>
    implements FqFlows<T> {

    private final Name name;

    private final Fqs<T> fqs;

    private final List<Flow<T>> flows;

    private final Duration timeout;

    private final Runner<T> runner;

    DefaultFqFlows(
        Name name,
        Fqs<T> fqs,
        Duration timeout,
        Runner<T> runner
    ) {
        this(name, fqs, timeout, runner, null);
    }

    DefaultFqFlows(
        Name name,
        Fqs<T> fqs,
        Duration timeout,
        Runner<T> runner,
        List<Flow<T>> flows
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.runner = runner;
        if (this.name.isBlank()) {
            throw new IllegalArgumentException("Could not create flows with empty name");
        }
        this.fqs = Objects.requireNonNull(fqs, "fqs");
        this.timeout = timeout;
        this.flows = flows == null || flows.isEmpty()
            ? List.of()
            : List.copyOf(flows);
    }

    public To<T> from(Name from) {
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
            flows.forEach(flow ->
                scope.fork(() ->
                    runner.run(this.name, fqs, flow)
                ));
            var count = scope.join().count();
            assert count == flows.size() : "Expected " + flows.size() + " runs, got " + count;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Flow execution interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute flow", e);
        }
    }

    private FqFlows<T> with(List<Flow<T>> flows) {
        return new DefaultFqFlows<>(
            name,
            fqs,
            timeout,
            runner,
            flows
        );
    }

    private Configuration configure(Configuration cf) {
        var named = cf
            .withName(name.name())
            .withThreadFactory(threadFactory(name));
        return timeout == null ? named
            : named.withTimeout(Duration.ofMinutes(5));
    }

    private FqFlows<T> withFlow(Flow<T> flow) {
        return with(Stream.concat(
                flows.stream(),
                Stream.of(flow)
            )
            .toList());
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

    private static ThreadFactory threadFactory(Name name) {
        LongAdder counter = new LongAdder();
        return runnable -> {
            try {
                return Thread.ofVirtual()
                    .name("%s-%s".formatted(name.name(), counter))
                    .unstarted(runnable);
            } finally {
                counter.increment();
            }
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ": " + runner + "]";
    }

    interface Runner<T> {

        void run(Name source, Fqs<T> fqs, Flow<T> flow);
    }
}
