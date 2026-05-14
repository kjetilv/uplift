package com.github.kjetilv.uplift.edam.patterns;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import static java.util.Objects.requireNonNull;

public record Occurrence<H extends HashKind<H>>(Instant time, Hash<H> hash, long sn)
    implements Comparable<Occurrence<H>>, Supplier<Hash<H>>, Spanning {

    public Occurrence(Instant time, Hash<H> hash) {
        this(time, hash, Sns.next());
    }

    public Occurrence {
        requireNonNull(time, "time");
        requireNonNull(hash, "id");
    }

    @Override
    public int compareTo(Occurrence occurrence) {
        return time().compareTo(occurrence.time());
    }

    @Override
    public Timespan timespan() {
        return new Timespan(time);
    }

    public boolean onOrAfter(Instant time) {
        return time == null || this.time.compareTo(time) >= 0;
    }

    public boolean before(Instant time) {
        return time == null || this.time.compareTo(time) < 0;
    }

    public boolean matches(Hash<H> hash) {
        return hash == null || this.hash.equals(hash);
    }

    @Override
    public Hash<H> get() {
        return hash();
    }

    private static final class Sns {

        private static final LongAdder sns = new LongAdder();

        private static long next() {
            try {
                return sns.longValue();
            } finally {
                sns.increment();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Occurrence<?> that &&
               Objects.equals(hash, that.hash) &&
               Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, hash);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + hash + "@" + time.truncatedTo(ChronoUnit.MILLIS) + "#" + sn + "]";
    }
}
