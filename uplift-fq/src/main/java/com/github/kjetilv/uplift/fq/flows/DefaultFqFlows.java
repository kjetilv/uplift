package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.StructuredTaskScope.Configuration;
import java.util.concurrent.StructuredTaskScope.Joiner;
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
    public Run feed() {
        return run(-1);
    }

    @Override
    public Run feed(Stream<T> items) {
        return run(feedItems(items));
    }

    private Run run(long itemCount) {
        var future =
            CompletableFuture.supplyAsync(this::runFlows, EXECUTOR);
        return new Run() {

            @Override
            public long count() {
                return itemCount;
            }

            @Override
            public Run join() {
                future.join();
                return this;
            }
        };
    }

    private int runFlows() {
        try (
            var scope = StructuredTaskScope.open(
                Joiner.allSuccessfulOrThrow(),
                this::configure
            )
        ) {
            for (var flow : flows) {
                scope.fork(run(flow));
            }
            return Math.toIntExact(scope.join().count());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Flow execution interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute flows", e);
        }
    }

    private Runnable run(Flow<T> flow) {
        try {
            return () ->
                runner.run(flow.fromOr(this.name), fqs, flow);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute " + flow, e);
        }
    }

    private long feedItems(Stream<T> items) {
        var counter = new LongAdder();
        try (var writer = fqs.writer(this.name)) {
             items.forEach(item -> {
                try {
                    writer.write(item);
                } finally {
                    counter.increment();
                }
            });
        }
        return counter.longValue();
    }

    private Configuration configure(Configuration cf) {
        var named = cf
            .withName(name.name())
            .withThreadFactory(threadFactory(name));
        return timeout != null
            ? named.withTimeout(Duration.ofMinutes(5))
            : named;
    }

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private static ThreadFactory threadFactory(Name name) {
        var counter = new LongAdder();
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
