package com.github.kjetilv.uplift.lambda;

import java.io.Closeable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Streamer<T> implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(Streamer.class);

    private final Supplier<Optional<T>> provider;

    private final AtomicBoolean closed = new AtomicBoolean();

    Streamer(Supplier<Optional<T>> provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            log.info("Closed: {}", this);
        }
    }

    Stream<T> open() {
        return closed.get()
            ? Stream.empty()
            : StreamSupport.stream(new SupplierSpliterator<T>(provider, closed::get), false);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + provider + (closed.get() ? ", closed" : "") + "]";
    }
}
