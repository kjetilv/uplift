package com.github.kjetilv.uplift.json.bytes;

import module java.base;

public final class MemorySegmentIntsBytesSource extends AbstractIntsBytesSource {

    private final MemorySegment segment;

    private int i;

    private final long len;

    public MemorySegmentIntsBytesSource(MemorySegment segment) {
        this.segment = Objects.requireNonNull(segment, "bytes");
        this.len = segment.byteSize();
        super();
    }

    @Override
    protected byte nextByte() {
        return i == len ? 0 : segment.get(JAVA_BYTE, i++);
    }

    private static final ValueLayout.OfByte JAVA_BYTE = ValueLayout.OfByte.JAVA_BYTE;
}
