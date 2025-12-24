package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;
import com.github.kjetilv.uplift.fq.data.Name;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Configuration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;
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
        Runner<T> runner,
        List<Flow<T>> flows
    ) {
        this.name = Objects.requireNonNull(name, "name");
        if (this.name.isBlank()) {
            throw new IllegalArgumentException("Could not create flows with empty name");
        }
        this.runner = Objects.requireNonNull(runner, "runner");
        this.fqs = Objects.requireNonNull(fqs, "fqs");
        this.timeout = timeout;
        this.flows = List.copyOf(flows);
        if (this.flows.isEmpty()) {
            throw new IllegalArgumentException("No flows defined");
        }
    }

    @Override
    public void feed(Stream<T> items) {
        if (flows.isEmpty()) {
            throw new IllegalStateException("No flows defined");
        }
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
                scope.fork(() -> {
                        try {
                            runner.run(this.name, fqs, flow);
                        } catch (Exception e) {
                            throw new IllegalStateException("Failed to execute " + flow, e);
                        }
                    }
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

    private Configuration configure(Configuration cf) {
        var named = cf
            .withName(name.name())
            .withThreadFactory(threadFactory(name));
        return timeout != null
            ? named.withTimeout(Duration.ofMinutes(5))
            : named;
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

    public interface Runner<T> {

        void run(Name source, Fqs<T> fqs, Flow<T> flow);
    }
}
