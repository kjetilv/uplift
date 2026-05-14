package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

abstract class AbstractStorage<H extends HashKind<H>> implements Storage<H> {

    private long index;

    private boolean full;

    private final Window window;

    protected AbstractStorage(Window window) {
        this.window = Objects.requireNonNull(window, "window");
    }

    @Override
    public final Occurrence<H> apply(long index) {
        return get(index);
    }

    @Override
    public final Occurrence<H> get(long index) {
        return retrieveFrom(full ? (this.index + index) % window.count() : index);
    }

    @Override
    public final Storage<H> store(Collection<Occurrence<H>> occurrences) {
        for (var occurrence : occurrences) {
            try {
                storeTo(index, occurrence);
            } finally {
                index = modInc(index);
                full = full || index == 0;
            }
        }
        return this;
    }

    @Override
    public final Occurrence<H> getFirst() {
        return get(0);
    }

    @Override
    public final Occurrence<H> getLast() {
        return get(count() - 1);
    }

    @Override
    public final boolean isEmpty() {
        return count() == 0;
    }

    @Override
    public final Cursor<H> rewind() {
        return rewind(null);
    }

    @Override
    public final Cursor<H> forward() {
        return forward(null);
    }

    @Override
    public final Cursor<H> rewind(Hash<H> hash) {
        var count = count();
        return count == 0 ? NullCursor.create() : cursor(
            hash,
            count - 1,
            i -> i < 0,
            -1,
            Occurrence::onOrAfter
        );
    }

    @Override
    public final Cursor<H> forward(Hash<H> hash) {
        var count = count();
        return count == 0 ? NullCursor.create() : cursor(
            hash,
            0,
            i -> i == count,
            1,
            Occurrence::before
        );
    }

    @Override
    public final long count() {
        return full ? window.count() : index;
    }

    protected abstract Occurrence<H> retrieveFrom(long index);

    protected abstract void storeTo(long index, Occurrence<H> occurrence);

    private Cursor<H> cursor(
        Hash<H> hash,
        long index,
        LongPredicate indexCutoff,
        int step,
        BiPredicate<Occurrence<H>, Instant> timeCutoff
    ) {
        return new CursorImpl<>(
            hash,
            lastTime(),
            index,
            this::get,
            indexCutoff,
            step,
            timeCutoff
        );
    }

    private Instant lastTime() {
        return getLast().time().minus(window.duration());
    }

    private long modInc(long l) {
        return (l + 1L) % window.count();
    }
}
