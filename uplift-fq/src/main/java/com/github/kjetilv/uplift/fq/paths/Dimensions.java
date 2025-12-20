package com.github.kjetilv.uplift.fq.paths;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static java.text.MessageFormat.format;

public final class Dimensions {

    private final int start;

    private final int end;

    private final int max;

    private final long maxValue;

    private final int[] powers;

    private final int[] expt10s;

    private final String format;

    private final Map<Long, Ledge> ledges = new ConcurrentHashMap<>();

    public Dimensions(int start, int end, int max) {
        if (start < 0 || end < 0 || max < 0) {
            throw new IllegalArgumentException("Require non-negative start/end/max: " + start + "/" + end + "/" + max);
        }
        if (start >= end || end > max) {
            throw new IllegalArgumentException("Require " + start + "<" + end + "<=" + max);
        }
        this.start = start;
        this.end = end;
        this.max = max;
        this.powers = IntStream.range(start, end + 1).toArray();
        this.expt10s = IntStream.range(start, end + 1)
            .map(i -> (int) Math.pow(10, i))
            .toArray();
        this.maxValue = (long) Math.pow(10, max);
        this.format = format("%0{0}d", max);
    }

    Ledge ledge(long count) {
        if (count > maxValue) {
            throw new IllegalArgumentException("#" + count + " exceeds max " + maxValue);
        }
        var number = ledgeNumber(count);
        return ledges.computeIfAbsent(
            number,
            _ -> new LedgeImpl(number, String.format(format, number))
        );
    }

    private long ledgeNumber(long no) {
        var power = (int) Math.floor(Math.log10(no));
        var i = Arrays.binarySearch(powers, power);
        if (i < 0) {
            return normalize(no, expt10s[powers.length - 1]);
        }
        return normalize(no, expt10s[i]);
    }

    private long normalize(long no, int ten) {
        return no / ten * ten;
    }

    private record LedgeImpl(long number, String asSegment) implements Ledge {

        @Override
        public boolean equals(Object object) {
            return object instanceof LedgeImpl l && number == l.number;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(number);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + start + "<" + end + "<=" + max + "]";
    }
}
