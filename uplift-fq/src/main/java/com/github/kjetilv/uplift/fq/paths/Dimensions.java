package com.github.kjetilv.uplift.fq.paths;

import java.util.HashMap;
import java.util.Map;
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

    private final Map<Long, Ledge> ledges = new HashMap<>();

    public Dimensions(int start, int end, int max) {
        if (start < end && end <= max) {
            this.start = start;
            this.end = end;
            this.max = max;
        } else {
            throw new IllegalArgumentException("Require " + start + "<" + end + "<=" + max);
        }
        this.powers = IntStream.range(start, end + 1).toArray();
        this.expt10s = IntStream.range(start, end + 1)
            .map(i -> (int) Math.pow(10, i))
            .toArray();
        this.maxValue = (long) Math.pow(10, max);
        this.format = format("%0{0}d", max);
    }

    public Ledge ledge(long count) {
        if (count > maxValue) {
            throw new IllegalArgumentException("#" + count + " exceeds max " + maxValue);
        }
        return ledges.computeIfAbsent(
            ledgeNumber(count),
            no ->
                new LedgeImpl(
                    no,
                    String.format(format, no)
                )
        );
    }

    private long ledgeNumber(long no) {
        var power = (long) Math.floor(Math.log10(no));
        for (int i = 0; i < powers.length; i++) {
            if (power == powers[i]) {
                return normalize(no, expt10s[i]);
            }
        }
        return normalize(no, expt10s[powers.length - 1]);
    }

    private long normalize(long no, int ten) {
        return no / ten * ten;
    }

    private record LedgeImpl(long no, String segment) implements Ledge {

        @Override
        public boolean equals(Object object) {
            return object instanceof LedgeImpl l && no == l.no;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(no);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + start + "<" + end + "<=" + max + "]";
    }
}
