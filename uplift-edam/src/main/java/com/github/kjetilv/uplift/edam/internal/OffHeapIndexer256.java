package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module jdk.incubator.vector;
import module uplift.edam;
import module uplift.hash;
import module uplift.util;
import com.github.kjetilv.uplift.hash.HashKind.K256;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.MemoryLayout.sequenceLayout;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

/// Uses an off-heap memory segment to store hashes.
final class OffHeapIndexer256 extends AbstractOffHeapIndexer<K256> {

    private final MemorySegment segment;

    OffHeapIndexer256(Arena arena, HashFun<Hash<?>> hashFunction, long count) {
        super(hashFunction, count);
        this.segment = Objects.requireNonNull(arena, "arena")
            .allocate(sequenceLayout(hashFunction.slotCount(count), HL));
    }

    @Override
    protected Slot<K256> slot(long index) {
        return new SegmentSlot256(segment.asSlice(HL.byteSize() * index, HL));
    }

    private static final MemoryLayout HL = structLayout(JAVA_LONG, JAVA_LONG, JAVA_LONG, JAVA_LONG);

    private static final VarHandle H0 = HL.varHandle(groupElement(0));

    private static final VarHandle H1 = HL.varHandle(groupElement(1));

    private static final VarHandle H2 = HL.varHandle(groupElement(2));

    private static final VarHandle H3 = HL.varHandle(groupElement(3));

    record SegmentSlot256(MemorySegment slice) implements Slot<K256> {

        @Override
        public Hash<K256> load() {
            return Hashes.ofNullable(
                (long) H0.get(slice, 0),
                (long) H1.get(slice, 0),
                (long) H2.get(slice, 0),
                (long) H3.get(slice, 0)
            );
        }

        @Override
        public void store(Hash<K256> hash) {
            long[] ls = hash.ls();
            H0.set(slice, 0, ls[0]);
            H1.set(slice, 0, ls[1]);
            H2.set(slice, 0, ls[2]);
            H3.set(slice, 0, ls[3]);
        }
    }
}
