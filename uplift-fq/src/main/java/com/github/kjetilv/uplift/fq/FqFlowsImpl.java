package com.github.kjetilv.uplift.fq;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Configuration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FqFlowsImpl<T> implements FqFlows<T> {

    private final String name;

    private final Fqs<T> fqs;

    private final int batchSize;

    private final Duration timeout;

    private final List<Flow<T>> flows;

    private final Handler<T> handler;

    FqFlowsImpl(String name, Fqs<T> fqs) {
        this(name, fqs, null, 0, null, null);
    }

    private FqFlowsImpl(
        String name,
        Fqs<T> fqs,
        List<Flow<T>> flows,
        int batchSize,
        Duration timeout,
        Handler<T> handler
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.fqs = Objects.requireNonNull(fqs, "fqs");
        this.batchSize = Math.max(batchSize, 1);
        this.timeout = timeout;
        if (name.isBlank()) {
            throw new IllegalArgumentException("Could not create flows with empty name");
        }
        this.flows = flows == null || flows.isEmpty()
            ? List.of()
            : List.copyOf(flows);
        this.handler = handler;
    }

    @Override
    public To<T> from(String from) {
        return to ->
            process ->
                withFlow(new Flow<>(from, to, process));
    }

    @Override
    public void feed(Stream<T> items) {
        validateFlows();
        try (var writer = fqs.writer(this.name)) {
            items.forEach(writer::write);
        }
        try (
            var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.allSuccessfulOrThrow(),
                this::configure
            )
        ) {
            try {
                flows.forEach(flow ->
                    scope.fork(() ->
                        run(flow)));
                var count = scope.join().count();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Flow execution interrupted", e);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to execute flow", e);
            }
        }
    }

    @Override
    public FqFlows<T> onException(Handler<T> handler) {
        return new FqFlowsImpl<>(name, fqs, flows, batchSize, timeout, handler);
    }

    @Override
    public FqFlows<T> timeout(Duration timeout) {
        return new FqFlowsImpl<>(name, fqs, flows, batchSize, timeout, handler);
    }

    @Override
    public FqFlows<T> batchSize(int batchSize) {
        return new FqFlowsImpl<>(name, fqs, flows, batchSize, timeout, handler);
    }

    private void run(Flow<T> flow) {
        try (var writer = fqs.writer(flow.to())) {
            var source = flow.fromOr(this.name);
            if (batchSize > 1) {
                var listStream = fqs.batcher(source, batchSize)
                    .read()
                    .flatMap(items ->
                        process(flow, items));
                listStream
                    .forEach(writer::write);
            } else {
                fqs.streamer(source)
                    .read()
                    .flatMap(item ->
                        process(flow, item))
                    .forEach(writer::write);
            }
        }
    }

    private Stream<T> process(Flow<T> flow, T item) {
        try {
            return Stream.of(flow.processor().process(item));
        } catch (Exception e) {
            handler.failed(flow, item, e);
            return Stream.empty();
        }
    }

    private Stream<List<T>> process(Flow<T> flow, List<T> items) {
        try {
            return Stream.of(flow.processor().process(items));
        } catch (Exception e) {
            handler.failed(flow, items, e);
            return Stream.empty();
        }
    }

    private Configuration configure(Configuration cf) {
        var named = cf
            .withName(name)
            .withThreadFactory(threadFactory(name));
        return timeout == null ? named
            : named.withTimeout(Duration.ofMinutes(5));
    }

    private FqFlowsImpl<T> withFlow(Flow<T> flow) {
        return new FqFlowsImpl<>(
            name,
            fqs,
            Stream.concat(flows.stream(), Stream.of(flow))
                .toList(), batchSize,
            timeout, handler
        );
    }

    private void validateFlows() {
        if (flows.isEmpty()) {
            throw new IllegalStateException(this + ": No flows defined");
        }
        var sources = flows.stream()
            .filter(Flow::isFromSource)
            .toList();
        if (sources.isEmpty()) {
            throw new IllegalStateException(this + ": No source intake defined: " + print(flows));
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
            throw new IllegalStateException(this + ": Missing inputs to flows: " + print(missingInputs));
        }
    }

    private String print(List<Flow<T>> flows) {
        return flows.stream()
            .map(Flow::description)
            .collect(Collectors.joining(" "));
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
