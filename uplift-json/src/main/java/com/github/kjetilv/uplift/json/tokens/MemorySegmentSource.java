package com.github.kjetilv.uplift.json.tokens;

import java.lang.foreign.MemorySegment;
import java.util.function.IntSupplier;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public final class MemorySegmentSource extends AbstractBytesSource {

    public MemorySegmentSource(MemorySegment memorySegment, long startIndex, long endIndex) {
        super(reader(memorySegment, startIndex, endIndex));
    }

    public static final int ALIGNMENT = Math.toIntExact(JAVA_LONG.byteSize());

    private static IntSupplier reader(MemorySegment memorySegment, long startIndex, long endIndex) {
        return new IntSupplier() {

            private final long length = endIndex - startIndex;

            private final long head = startIndex % ALIGNMENT;

            private final long tail = endIndex % ALIGNMENT;

            private final long preamble = head == 0L ? 0L : ALIGNMENT - head;

            private final long lastLongIndex = length - tail;

            private long segmentOffset;

            private int longOffset;

            private long currentLong;

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
        };
    }

}
