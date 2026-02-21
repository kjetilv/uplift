package com.github.kjetilv.uplift.util;

import module java.base;

public record Sleeper(State state, LongConsumer doze, Consumer<State> onMax) {

    public static Supplier<Sleeper> deferred(Supplier<String> description) {
        return deferred(description, (Consumer<State>) null);
    }

    public static Supplier<Sleeper> deferred(Supplier<String> description, Consumer<State> onMax) {
        return deferred(description, null, null, onMax);
    }

    public static Supplier<Sleeper> deferred(Supplier<String> description, Duration timeout) {
        return deferred(description, timeout, null, null);
    }

    public static Supplier<Sleeper> deferred(
        Supplier<String> description,
        LongConsumer doze
    ) {
        return deferred(description, null, doze, null);
    }

    public static Supplier<Sleeper> deferred(
        Supplier<String> description,
        LongConsumer doze,
        Consumer<State> onMax
    ) {
        return deferred(description, null, doze, onMax);
    }

    public static Supplier<Sleeper> deferred(
        Supplier<String> description,
        LongConsumer doze,
        Duration timeout
    ) {
        return deferred(description, timeout, doze, null);
    }

    public static Supplier<Sleeper> deferred(
        Supplier<String> description,
        Duration timeout,
        Consumer<State> onMax
    ) {
        return deferred(description, timeout, null, onMax);
    }

    public static Supplier<Sleeper> deferred(
        Supplier<String> description,
        Duration timeout,
        LongConsumer doze,
        Consumer<State> onMax
    ) {
        return StableValue.supplier(() ->
            new Sleeper(
                description.get(),
                0L,
                0L,
                doze,
                timeout,
                onMax
            ));
    }

    private Sleeper(
        String description,
        double time,
        long maxTime,
        LongConsumer doze,
        Duration timeout,
        Consumer<State> onMax
    ) {
        var now = Instant.now();
        var deadline = timeout == null ? null : now.plus(timeout);
        var state = new State(
            description,
            time > 0d ? time : MIN_SLEEP,
            maxTime > 0 ? maxTime : MAX_SLEEP,
            0L,
            now,
            deadline
        );
        this(state, doze == null ? defaultDoze() : doze, onMax);
    }

    public void sleep() {
        doze.accept(Math.round(state.time));
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
        new Sleeper(nextState, doze, onMax);
    }

    private static final double MAX_SLEEP = 10d;

    private static final double MIN_SLEEP = 0.5d;

    private static final double SQRT_2 = Math.sqrt(2d);

    private static LongConsumer defaultDoze() {
        var lock = new boolean[0];
        return ms -> {
            synchronized (lock) {
                try {
                    lock.wait(ms);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted", e);
                }
            }
        };
    }

    public record State(
        String description,
        double time,
        double maxTime,
        double slept,
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
                Math.min(time * SQRT_2, maxTime),
                maxTime,
                slept + time,
                starting,
                deadline
            );
        }

        private boolean onMaxTime() {
            return time >= maxTime;
        }
    }
}
