package com.github.kjetilv.uplift.util;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record Sleeper(State state, Object lock, Consumer<State> onMax) {

    public static Supplier<Sleeper> deferred(Supplier<String> description) {
        return deferred(description, (Consumer<State>) null);
    }

    public static Supplier<Sleeper> deferred(Supplier<String> description, Consumer<State> onMax) {
        return deferred(description, null, onMax);
    }

    public static Supplier<Sleeper> deferred(Supplier<String> description, Duration timeout) {
        return deferred(description, timeout, null);
    }

    public static Supplier<Sleeper> deferred(
        Supplier<String> description,
        Duration timeout,
        Consumer<State> onMax
    ) {
        return StableValue.supplier(() ->
            new Sleeper(
                description.get(),
                0L,
                0L,
                timeout,
                onMax
            ));
    }

    private Sleeper(
        String description,
        long time,
        long maxTime,
        Duration timeout,
        Consumer<State> onMax
    ) {
        var now = Instant.now();
        var deadline = timeout == null ? null : now.plus(timeout);
        var state = new State(
            description,
            time > 0 ? time : MIN_SLEEP,
            maxTime > 0 ? maxTime : MAX_SLEEP,
            now,
            deadline
        );
        this(state, newLock(), onMax);
    }

    public void sleep() {
        synchronized (lock) {
            try {
                lock.wait(state.time);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", e);
            }
        }
        if (state.onMaxTime()) {
            return;
        }
        var nextState = state.increasedTime();
        if (nextState.onMaxTime()) {
            nextState.handleTimeout();
            if (onMax != null) {
                onMax.accept(nextState);
            }
        }
        new Sleeper(nextState, lock, onMax);
    }

    private static final long MAX_SLEEP = 100L;

    private static final long MIN_SLEEP = 1L;

    private static Object newLock() {
        return new boolean[0];
    }

    public record State(
        String description,
        long time,
        long maxTime,
        Instant starting,
        Instant deadline
    ) {

        public Duration duration() {
            var now = Instant.now();
            return Duration.between(starting, now);
        }

        private void handleTimeout() {
            if (deadline != null) {
                var now = Instant.now();
                if (now.isAfter(deadline)) {
                    var time = Duration.between(starting, now);
                    throw new IllegalStateException("Timed out after " + time + ": " + description);
                }
            }
        }

        private State increasedTime() {
            return new State(
                description,
                Math.min(time * 2, maxTime),
                maxTime,
                starting,
                deadline
            );
        }

        private boolean onMaxTime() {
            return time >= maxTime;
        }
    }
}
