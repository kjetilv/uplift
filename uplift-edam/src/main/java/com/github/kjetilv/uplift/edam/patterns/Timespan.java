package com.github.kjetilv.uplift.edam.patterns;

import module java.base;

@SuppressWarnings("unused")
public record Timespan(Instant start, Instant end) implements Spanning, Comparable<Timespan> {

    public static Timespan of(Spanning... spannings) {
        return of(Stream.of(spannings));
    }

    public static <C extends Collection<? extends Spanning>> Timespan of(C spannings) {
        return of(spannings.stream());
    }

    static Timespan of(Stream<? extends Spanning> spannings) {
        return spannings.reduce(
            NONE,
            (timespan, spanning) ->
                timespan.and(spanning.timespan()),
            Timespan::and
        );
    }

    public Timespan(Instant start) {
        this(start, start);
    }

    public Timespan {
        if (start == null ^ end == null) {
            throw new IllegalArgumentException("start/end must be null or both not null: " + start + "/" + end);
        }
    }

    @Override
    public Timespan timespan() {
        return this;
    }

    @Override
    public int compareTo(Timespan other) {
        return start.compareTo(other.start());
    }

    boolean isInstant() {
        return Objects.equals(start, end);
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    Timespan and(Timespan timespan) {
        if (timespan == null || timespan.equals(NONE)) {
            return this;
        }
        Instant otherStart = timespan.start;
        Instant otherEnd = timespan.end;
        return new Timespan(
            start == null || otherStart.isBefore(start)
                ? otherStart
                : start,
            end == null || otherEnd.isAfter(end)
                ? otherEnd
                : end
        );
    }

    static final Timespan NONE = new Timespan(null);
}
