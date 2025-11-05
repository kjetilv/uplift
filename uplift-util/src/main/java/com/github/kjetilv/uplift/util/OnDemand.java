package com.github.kjetilv.uplift.util;

import module java.base;

public record OnDemand(Supplier<Instant> clock) {

    public <T> void force(Supplier<T> supplier, T t) {
        if (supplier instanceof BuilderImpl<T>.ResettableSupplier resettableSupplier) {
            resettableSupplier.force(t);
        } else {
            throw new IllegalArgumentException("Not a resettable supplier: " + supplier);
        }
    }

    public void reset(Supplier<?>... suppliers) {
        for (var supplier : suppliers) {
            if (supplier instanceof BuilderImpl<?>.ResettableSupplier builder) {
                builder.reset();
            } else {
                throw new IllegalArgumentException("Not a resettable supplier: " + supplier);
            }
        }
    }

    public <T> Builder<T> after(TemporalAmount refreshInterval) {
        return new BuilderImpl<>(
            clock,
            refreshInterval,
            new AtomicReference<>(Instant.EPOCH),
            new AtomicReference<>(),
            new AtomicBoolean()
        );
    }

    private record BuilderImpl<T>(
        Supplier<Instant> clock,
        TemporalAmount refreshInterval,
        AtomicReference<Instant> lastHolder,
        AtomicReference<T> holder,
        AtomicBoolean reset
    ) implements Builder<T> {

        @Override
        public Supplier<T> get(Supplier<? extends T> supplier) {
            return new ResettableSupplier(supplier);
        }

        private final class ResettableSupplier implements Supplier<T> {

            private final Supplier<? extends T> supplier;

            private ResettableSupplier(Supplier<? extends T> supplier) {
                this.supplier = supplier;
            }

            @Override
            public T get() {
                return holder.updateAndGet(existing -> {
                    if (existing == null) {
                        return supplier.get();
                    }
                    var time = clock.get();
                    if (reset.compareAndSet(true, false) || expiredAt(time)) {
                        lastHolder.set(time);
                        return supplier.get();
                    }
                    return existing;
                });
            }

            private void force(T newValue) {
                holder.updateAndGet(_ -> {
                    lastHolder.set(clock.get());
                    reset.set(false);
                    return newValue;
                });
            }

            private void reset() {
                reset.compareAndSet(false, true);
            }

            private boolean expiredAt(Instant time) {
                var last = lastHolder.get();
                var refreshTime = last.plus(refreshInterval);
                return time.equals(refreshTime) || time.isAfter(refreshTime);
            }
        }
    }

    @FunctionalInterface
    public interface Builder<T> {

        Supplier<T> get(Supplier<? extends T> supplier);
    }
}
