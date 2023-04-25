package com.github.kjetilv.uplift.lambda;

import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SupplierSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

    private static final Logger log = LoggerFactory.getLogger(SupplierSpliterator.class);

    private final Supplier<Optional<T>> provider;

    private final BooleanSupplier closed;

    SupplierSpliterator(Supplier<Optional<T>> provider, BooleanSupplier closed) {
        super(Long.MAX_VALUE, Spliterator.DISTINCT);
        this.provider = Objects.requireNonNull(provider, "provider");
        this.closed = Objects.requireNonNull(closed, "closed");
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (closed.getAsBoolean()) {
            return false;
        }
        Optional<T> next;
        try {
            next = provider.get();
        } catch (Exception e) {
            if (closed.getAsBoolean()) {
                log.debug("Failed to retrieve after close", e);
                return false;
            }
            throw new IllegalStateException("Failed to retrieve", e);
        }
        next.ifPresent(action);
        return next.isPresent();
    }
}
