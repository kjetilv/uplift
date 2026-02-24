package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module jdk.incubator.vector;
import com.github.kjetilv.uplift.edam.HashFun;
import com.github.kjetilv.uplift.hash.Hash;

import static com.github.kjetilv.uplift.hash.HashKind.K128;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.MemoryLayout.sequenceLayout;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

/// Uses an off-heap memory segment to store hashes.
final class OffHeapIndexer128 extends AbstractOffHeapIndexer<K128> {

    private final MemorySegment segment;

    OffHeapIndexer128(Arena arena, HashFun<Hash<?>> hashFunction, long count) {
        super(hashFunction, count);
        this.segment = Objects.requireNonNull(arena, "arena")
            .allocate(sequenceLayout(hashFunction.slotCount(count), HL));
    }

    @Override
    protected Slot<K128> slot(long index) {
        return new SegmentSlot128(segment.asSlice(HL.byteSize() * index, HL));
    }

    private static final MemoryLayout HL = structLayout(JAVA_LONG, JAVA_LONG);

    private static final VarHandle H0 = HL.varHandle(groupElement(0));

    private static final VarHandle H1 = HL.varHandle(groupElement(1));

    record SegmentSlot128(MemorySegment slice) implements Slot<K128> {

        @Override
        public Hash<K128> load() {
            return K128.of(
                (long) H0.get(slice, 0),
                (long) H1.get(slice, 0)
            );
        }

        @Override
        public void store(Hash<K128> hash) {
            var ls = hash.ls();
            H0.set(slice, 0, ls[0]);
            H1.set(slice, 0, ls[1]);
        }
    }
}
