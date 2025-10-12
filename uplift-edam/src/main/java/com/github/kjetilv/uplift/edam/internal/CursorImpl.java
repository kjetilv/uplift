package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module uplift.edam;
import module uplift.hash;

final class CursorImpl<K extends HashKind<K>> implements Storage.Cursor<K> {

    private long index;

    private final Hash<K> hash;

    private final Instant cutoffTime;

    private final Function<Long, Occurrence<K>> getter;

    private final LongPredicate indexCutoff;

    private final int step;

    private final BiPredicate<Occurrence<K>, Instant> timeCutoff;

    CursorImpl(
        Hash<K> hash,
        Instant cutoffTime,
        long index,
        Function<Long, Occurrence<K>> getter,
        LongPredicate indexCutoff,
        int step,
        BiPredicate<Occurrence<K>, Instant> timeCutoff
    ) {
        this.hash = hash;
        this.cutoffTime = cutoffTime;
        this.index = index;
        this.getter = Objects.requireNonNull(getter, "getter");
        this.indexCutoff = indexCutoff;
        this.step = step;
        this.timeCutoff = timeCutoff;
    }

    @Override
    public Optional<Occurrence<K>> next() {
        while (true) {
            if (indexCutoff.test(index)) {
                return Optional.empty();
            }
            try {
                var occurrence = getter.apply(index);
                if (occurrence.matches(hash) && occurrence.onOrAfter(cutoffTime)) {
                    return Optional.of(occurrence);
                }
            } finally {
                index += step;
            }
        }
    }

    @Override
    public Stream<Occurrence<K>> spool(Instant limit) {
        return stream(
            this::next,
            occurrence ->
                timeCutoff.test(occurrence, limit)
        );
    }
}
