package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.LongPredicate;

abstract class AbstractStorage<K extends HashKind<K>> implements Storage<K> {

    private long index;

    private boolean full;

    private final int count;

    private final Duration duration;

    protected AbstractStorage(Window window) {
        Objects.requireNonNull(window, "window");
        this.count = window.count();
        this.duration = window.duration();
    }

    @Override
    public final Occurrence<K> apply(long index) {
        return get(index);
    }

    @Override
    public final Occurrence<K> get(long index) {
        return retrieveFrom(full ? (this.index + index) % count : index);
    }

    @Override
    public final Storage<K> store(Collection<Occurrence<K>> occurrences) {
        for (Occurrence<K> occurrence : occurrences) {
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
    public final Occurrence<K> getFirst() {
        return get(0);
    }

    @Override
    public final Occurrence<K> getLast() {
        return get(count() - 1);
    }

    @Override
    public final boolean isEmpty() {
        return count() == 0;
    }

    @Override
    public final Cursor<K> rewind() {
        return rewind(null);
    }

    @Override
    public final Cursor<K> forward() {
        return forward(null);
    }

    @Override
    public final Cursor<K> rewind(Hash<K> hash) {
        long count = count();
        return count == 0 ? NullCursor.create() : cursor(
            hash,
            count - 1,
            i -> i < 0,
            -1,
            Occurrence::onOrAfter
        );
    }

    @Override
    public final Cursor<K> forward(Hash<K> hash) {
        long count = count();
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
        return full ? count : index;
    }

    protected abstract Occurrence<K> retrieveFrom(long index);

    protected abstract void storeTo(long index, Occurrence<K> occurrence);

    private Cursor<K> cursor(
        Hash<K> hash,
        long index,
        LongPredicate indexCutoff,
        int step,
        BiPredicate<Occurrence<K>, Instant> timeCutoff
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
        return getLast().time().minus(duration);
    }

    private long modInc(long l) {
        return (l + 1L) % count;
    }
}
