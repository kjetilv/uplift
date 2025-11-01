package com.github.kjetilv.uplift.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class SupplierSpliterator<T> extends Spliterators.AbstractSpliterator<CompletionStage<T>> implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(SupplierSpliterator.class);

    private final Supplier<Optional<CompletionStage<T>>> provider;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final LongAdder retrieved = new LongAdder();

    private final LongAdder failedRetrieve = new LongAdder();

    private final LongAdder completed = new LongAdder();

    private final LongAdder failedComplete = new LongAdder();

    SupplierSpliterator(Supplier<Optional<CompletionStage<T>>> provider) {
        super(Long.MAX_VALUE, DISTINCT);
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    @Override
    public boolean tryAdvance(Consumer<? super CompletionStage<T>> action) {
        if (closed.get()) {
            return false;
        }
        return getNext()
            .map(next ->
                next.whenComplete((_, throwable) -> {
                    completed.increment();
                    if (throwable != null) {
                        failedComplete.increment();
                    }
                }))
            .map(stage ->
                accepted(action, stage))
            .orElse(false);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            log.info("Closed: {}", this);
        }
    }

    private Optional<CompletionStage<T>> getNext() {
        try {
            return provider.get();
        } catch (Exception e) {
            failedRetrieve.increment();
            if (closed.get()) {
                log.debug("Failed to retrieve after close", e);
                return Optional.empty();
            }
            throw new IllegalStateException("Failed to retrieve", e);
        } finally {
            retrieved.increment();
        }
    }

    private static <T> boolean accepted(Consumer<? super CompletionStage<T>> action, CompletionStage<T> stage) {
        action.accept(stage);
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               (closed.get() ? "[CLOSED] " : "") +
               "retrieved:" + retrieved + (failedRetrieve.sum() > 0 ? "(" + failedRetrieve + " failed)" : "") +
               " completed:" + completed + (failedComplete.sum() > 0 ? "(" + failedComplete + " failed)" : "") +
               "]";
    }
}
