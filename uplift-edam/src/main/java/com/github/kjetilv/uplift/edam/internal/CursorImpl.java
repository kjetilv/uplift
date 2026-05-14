package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

final class CursorImpl<H extends HashKind<H>> implements Storage.Cursor<H> {

    private long index;

    private final Hash<H> hash;

    private final Instant cutoffTime;

    private final Function<Long, Occurrence<H>> getter;

    private final LongPredicate indexCutoff;

    private final int step;

    private final BiPredicate<Occurrence<H>, Instant> timeCutoff;

    CursorImpl(
        Hash<H> hash,
        Instant cutoffTime,
        long index,
        Function<Long, Occurrence<H>> getter,
        LongPredicate indexCutoff,
        int step,
        BiPredicate<Occurrence<H>, Instant> timeCutoff
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
    public Optional<Occurrence<H>> next() {
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
    public Stream<Occurrence<H>> spool(Instant limit) {
        return stream(
            this::next,
            occurrence ->
                timeCutoff.test(occurrence, limit)
        );
    }
}
