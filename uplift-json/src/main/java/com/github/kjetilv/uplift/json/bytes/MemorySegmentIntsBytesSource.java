package com.github.kjetilv.uplift.json.bytes;

import module java.base;

/// Source over a memory segment, copied in chunks. Use [#of(MemorySegment)] to scan a heap segment over a byte
/// array in place.
public final class MemorySegmentIntsBytesSource extends AbstractIntsBytesSource {

    private final MemorySegment segment;

    private long next;

    public MemorySegmentIntsBytesSource(MemorySegment segment) {
        this.segment = Objects.requireNonNull(segment, "segment");
        super();
    }

    /// Creates a source for the segment: in place for a heap segment over a byte array, otherwise copying in
    /// chunks
    ///
    /// @param segment Segment
    /// @return Source
    public static AbstractIntsBytesSource of(MemorySegment segment) {
        return segment.heapBase()
            .filter(byte[].class::isInstance)
            .map(byte[].class::cast)
            .filter(_ -> segment.byteSize() <= Integer.MAX_VALUE)
            .<AbstractIntsBytesSource>map(array ->
                new ByteArrayIntsBytesSource(array, Math.toIntExact(segment.address()), (int) segment.byteSize()))
            .orElseGet(() -> new MemorySegmentIntsBytesSource(segment));
    }

    @Override
    protected int read(byte[] dst, int offset, int length) {
        var remaining = segment.byteSize() - next;
        if (remaining <= 0) {
            return -1;
        }
        var count = (int) Math.min(length, remaining);
        MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, next, dst, offset, count);
        next += count;
        return count;
    }
}
