package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.MemoryLayout.sequenceLayout;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

final class OffHeapStorage<K extends HashKind<K>> extends AbstractStorage<K> {

    static <K extends HashKind<K>> Storage<K> create(
        Window window,
        Indexer<Hash<K>> indexer,
        Arena arena
    ) {
        return new OffHeapStorage<>(
            window,
            indexer,
            arena
        );
    }

    private final MemorySegment segment;

    private final Indexer<Hash<K>> indexer;

    OffHeapStorage(Window window, Indexer<Hash<K>> indexer, Arena arena) {
        super(window);
        this.indexer = Objects.requireNonNull(indexer, "indexer");
        this.segment = Objects.requireNonNull(arena, "arena")
            .allocate(sequenceLayout(window.count(), OL));
    }

    @Override
    protected Occurrence<K> retrieveFrom(long index) {
        MemorySegment slice = slice(index);
        long i = (long) IH.get(slice, 0);
        long n = (long) NH.get(slice, 0);
        return new Occurrence<>(
            START_TIME.plusNanos(n),
            indexer.exchange(i)
        );
    }

    @Override
    protected void storeTo(long index, Occurrence<K> occurrence) {
        long i = indexer.exchange(occurrence.hash());
        long n = Duration.between(
            START_TIME,
            occurrence.time()
        ).toNanos();
        MemorySegment slice = slice(index);
        NH.set(slice, 0, n);
        IH.set(slice, 0, i);
    }

    private MemorySegment slice(long index) {
        return segment.asSlice(OLS * index, OL);
    }

    private static final Instant START_TIME = Instant.now();

    private static final StructLayout OL = structLayout(JAVA_LONG, JAVA_LONG);

    private static final VarHandle NH = OL.varHandle(groupElement(0));

    private static final VarHandle IH = OL.varHandle(groupElement(1));

    private static final long OLS = OL.byteSize();
}
