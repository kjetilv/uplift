package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.FqWriter;
import com.github.kjetilv.uplift.fq.Fqs;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Configuration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.fq.flows.DefaultFqFlows.Phase.*;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.StructuredTaskScope.Joiner.allSuccessfulOrThrow;

final class DefaultFqFlows<T> implements FqFlows<T>, Closeable {

    private final Name name;

    private final Fqs<T> fqs;

    private final List<Flow<T>> flows;

    private final Duration timeout;

    private final FlowRunner<T> runner;

    private final LongAdder counter = new LongAdder();

    private final StableValue<FqWriter<T>> sourceWriter = StableValue.of();

    private final AtomicReference<Phase> phase = new AtomicReference<>(NEW);

    private CompletableFuture<Void> flowsFuture;

    DefaultFqFlows(
        Name name,
        Fqs<T> fqs,
        Duration timeout,
        FlowRunner<T> runner,
        List<Flow<T>> flows
    ) {
        this.name = requireNonNull(name, "name");
        if (this.name.isBlank()) {
            throw new IllegalArgumentException("Could not create flows with empty name");
        }
        this.runner = requireNonNull(runner, "runner");
        this.fqs = requireNonNull(fqs, "fqs");
        this.timeout = timeout;
        this.flows = List.copyOf(flows);
        if (this.flows.isEmpty()) {
            throw new IllegalArgumentException("No flows defined");
        }
        Flows.validateAll(this.flows);
    }

    @Override
    public boolean start() {
        return started();
    }

    @Override
    public long feed(List<T> items) {
        if (items == null || items.isEmpty()) {
            return counter.longValue();
        }
        for (T item : items) {
            add(item);
        }
        return counter.longValue();
    }

    @Override
    public Run feed(Stream<T> items) {
        started();
        return run(() -> {
            try (this) {
                started();
                items.forEach(this::add);
            }
            return counter.longValue();
        });
    }

    @Override
    public Run run() {
        return run(() -> {
            try (this) {
                started();
            }
            return counter.longValue();
        });
    }

    @Override
    public void close() {
        closed();
    }

    private boolean started() {
        if (transition(NEW, STARTED)) {
            try {
                return true;
            } finally {
                startFlow();
            }
        }
        return false;
    }

    private void closed() {
        if (transition(STARTED, DONE)) {
            var sourceWriter = this.sourceWriter.orElse(null);
            if (sourceWriter != null) {
                sourceWriter.close();
            }
        }
    }

    private boolean transition(Phase previous, Phase next) {
        if (next.ordinal() == previous.ordinal() + 1) {
            return phase.compareAndSet(previous, next);
        }
        throw new IllegalStateException("Phase transition from " + previous + " to " + next + " not allowed");
    }

    private void startFlow() {
        this.flowsFuture = CompletableFuture.runAsync(() -> {
            try (
                var scope = StructuredTaskScope.open(
                    allSuccessfulOrThrow(),
                    configuration ->
                        withTimeout(configuration
                            .withName(name.name())
                            .withThreadFactory(threadFactory(name)))
                )
            ) {
                for (var flow : this.flows) {
                    init(flow);
                    scope.fork(execute(flow));
                }
                scope.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Flow execution interrupted", e);
            }
        });
    }

    private void init(Flow<T> flow) {
        fqs.init(flow.to());
    }

    @SuppressWarnings("resource")
    private void add(T item) {
        try {
            sourceWriter
                .orElseSet(() -> fqs.writer(this.name))
                .write(requireNonNull(item, "item"));
        } finally {
            counter.increment();
        }
    }

    private Runnable execute(Flow<T> flow) {
        var runnableFlow = flow.isFromSource()
            ? flow.from(this.name)
            : flow;
        try {
            return () ->
                runner.run(fqs, runnableFlow);
        } catch (Exception e) {
            throw new IllegalStateException(runner + " failed to execute " + runnableFlow, e);
        }
    }

    private DefaultRun run(LongSupplier itemCount) {
        return new DefaultRun(flowsFuture, itemCount);
    }

    private Configuration withTimeout(Configuration named) {
        return timeout == null ? named
            : named.withTimeout(Duration.ofMinutes(5));
    }

    private static ThreadFactory threadFactory(Name name) {
        var threadCount = new LongAdder();
        return runnable -> {
            try {
                return Thread.ofVirtual()
                    .name("%s-%s".formatted(name.name(), threadCount))
                    .unstarted(runnable);
            } finally {
                threadCount.increment();
            }
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ": " + runner + "]";
    }

    enum Phase {
        NEW,
        STARTED,
        DONE
    }
}
