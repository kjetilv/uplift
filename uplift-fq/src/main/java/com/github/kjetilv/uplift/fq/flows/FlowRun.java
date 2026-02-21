package com.github.kjetilv.uplift.fq.flows;

import module java.base;

public record FlowRun<T>(Flow<T> flow, Instant start, Instant end) {

    private Duration truncated(Duration duration) {
        if (duration.toHours() > 1) {
            return duration.truncatedTo(ChronoUnit.MINUTES);
        }
        if (duration.toMinutes() > 1) {
            return duration.truncatedTo(ChronoUnit.SECONDS);
        }
        if (duration.getSeconds() > 1) {
            return duration.truncatedTo(ChronoUnit.MILLIS);
        }
        return duration;
    }

    @Override
    public String toString() {
        Duration duration = truncated(Duration.between(start, end));
        return getClass().getSimpleName() + "[" + flow.description() + " " + start + "+" + duration + "]";
    }
}
