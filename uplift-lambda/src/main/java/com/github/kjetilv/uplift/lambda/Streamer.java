package com.github.kjetilv.uplift.lambda;

import module java.base;

final class Streamer<T> implements Closeable {

    private final AtomicBoolean opened = new AtomicBoolean();

    private final AtomicBoolean closed = new AtomicBoolean();

    private final SupplierSpliterator<T> spliterator;

    Streamer(Supplier<Optional<CompletionStage<T>>> provider) {
        this.spliterator = new SupplierSpliterator<>(Objects.requireNonNull(provider, "provider"));
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
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
        return getClass().getSimpleName() + "[" + spliterator +
               (opened.get() ? ", open" : "") +
               (closed.get() ? ", closed" : "") +
               "]";
    }
}
