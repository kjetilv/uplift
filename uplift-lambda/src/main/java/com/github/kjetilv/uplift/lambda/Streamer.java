package com.github.kjetilv.uplift.lambda;

import java.io.Closeable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Streamer<T> implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(Streamer.class);

    private final Supplier<Optional<CompletionStage<T>>> provider;

    private final AtomicBoolean opened = new AtomicBoolean();

    private final AtomicBoolean closed = new AtomicBoolean();

    private final SupplierSpliterator<T> spliterator;

    Streamer(Supplier<Optional<CompletionStage<T>>> provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.spliterator = new SupplierSpliterator<>(provider, closed::get);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)){
            spliterator.close();
        }
    }

    Stream<CompletionStage<T>> open() {
        if (opened.compareAndSet(false, true)) {
            return StreamSupport.stream(spliterator, false);
        }
        throw new IllegalStateException("Already opened: " + this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + provider +
               (opened.get() ? ", open" : "") +
               (closed.get() ? ", closed" : "") +
               "]";
    }
}
