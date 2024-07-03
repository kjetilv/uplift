package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.bits.Bits;
import com.github.kjetilv.flopp.kernel.bits.MemorySegments;
import com.github.kjetilv.uplift.json.tokens.AbstractBytesSource;

import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public final class MemorySegmentSource extends AbstractBytesSource {

    public MemorySegmentSource(MemorySegment memorySegment, long startIndex, long endIndex) {
        super(reader(memorySegment, startIndex, endIndex));
    }

    public MemorySegmentSource(LineSegment lineSegment) {
        super(reader(lineSegment));
    }

    private static final int ALIGNMENT = Math.toIntExact(JAVA_LONG.byteSize());

    private static IntSupplier reader(
        MemorySegment memorySegment,
        long startIndex,
        long endIndex
    ) {
        return new MemSegIntSupplier(endIndex, startIndex, memorySegment);
    }

    private static IntSupplier reader(LineSegment lineSegment) {
        return new IntSupplier() {

            private final LongSupplier longSupplier = lineSegment.longSupplier();

            private int index = 0;

            private long data = longSupplier.getAsLong();

            @Override
            public int getAsInt() {
                try {
                    return Bits.getByte(data, index);
                } finally {
                    index++;
                    if (index == MemorySegments.ALIGNMENT_INT) {
                        data = longSupplier.getAsLong();
                        index = 0;
                    }
                }
            }
        };
    }

    private static final class MemSegIntSupplier implements IntSupplier {

        private final long length;

        private final long preamble;

        private final long lastLongIndex;

        private long segmentOffset;

        private int longOffset;

        private long currentLong;

        private final long startIndex;

        private final MemorySegment memorySegment;

        private MemSegIntSupplier(long endIndex, long startIndex, MemorySegment memorySegment) {
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
