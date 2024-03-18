package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.uplift.json.tokens.AbstractBytesSource;

import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.IntSupplier;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public final class MemorySegmentSource extends AbstractBytesSource {

    public MemorySegmentSource(MemorySegment memorySegment, long startIndex, long endIndex) {
        super(reader(memorySegment, startIndex, endIndex));
    }

    private static final int ALIGNMENT = Math.toIntExact(JAVA_LONG.byteSize());

    private static IntSupplier reader(
        MemorySegment memorySegment,
        long startIndex,
        long endIndex
    ) {
        return new MemSeg(endIndex, startIndex, memorySegment);
    }

    private static final class MemSeg implements IntSupplier {

        private final long length;

        private final long preamble;

        private final long lastLongIndex;

        private long segmentOffset;

        private int longOffset;

        private long currentLong;

        private final long startIndex;

        private final MemorySegment memorySegment;

        private MemSeg(long endIndex, long startIndex, MemorySegment memorySegment) {
            this.memorySegment = Objects.requireNonNull(memorySegment, "memorySegment");
            if (startIndex < 0) {
                throw new IllegalArgumentException("startIndex < 0: " + startIndex);
            }
            if (endIndex < startIndex) {
                throw new IllegalArgumentException("endIndex < startIndex: " + endIndex + " < " + startIndex);
            }
            this.startIndex = startIndex;
            length = endIndex - startIndex;
            long head = startIndex % ALIGNMENT;
            long tail = endIndex % ALIGNMENT;
            preamble = head == 0L ? 0L : ALIGNMENT - head;
            lastLongIndex = length - tail;
        }

        @Override
        public int getAsInt() {
            if (segmentOffset >= length) {
                return -1;
            }
            if (segmentOffset < preamble || segmentOffset >= lastLongIndex) {
                byte value = memorySegment.get(JAVA_BYTE, startIndex + segmentOffset);
                segmentOffset++;
                return value;
            }
            if (longOffset == 0) {
                currentLong = memorySegment.get(JAVA_LONG, segmentOffset);
            }
            try {
                long value = currentLong & 0xFFL;
                currentLong >>= ALIGNMENT;
                return Math.toIntExact(value);
            } finally {
                segmentOffset++;
                longOffset += 1;
                longOffset %= ALIGNMENT;
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + memorySegment + " " + segmentOffset + "/" + length + "]";
        }
    }
}
