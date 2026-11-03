package com.github.kjetilv.uplift.json.bytes;

import module java.base;

/// Source over the remaining bytes of a buffer, from its position to its limit. The buffer's position is not
/// changed. A buffer backed by an array is scanned in place.
public final class ByteBufferIntsBytesSource extends AbstractIntsBytesSource {

    private final ByteBuffer bytes;

    private int next;

    public ByteBufferIntsBytesSource(ByteBuffer bytes) {
        this.bytes = bytes.slice();
        super();
    }

    /// Creates a source for the buffer: in place for an array-backed buffer, otherwise copying in chunks
    ///
    /// @param bytes Buffer
    /// @return Source
    public static AbstractIntsBytesSource of(ByteBuffer bytes) {
        return bytes.hasArray()
            ? new ByteArrayIntsBytesSource(bytes.array(), bytes.arrayOffset() + bytes.position(), bytes.remaining())
            : new ByteBufferIntsBytesSource(bytes);
    }

    @Override
    protected int read(byte[] dst, int offset, int length) {
        var remaining = bytes.limit() - next;
        if (remaining <= 0) {
            return -1;
        }
        var count = Math.min(length, remaining);
        bytes.get(next, dst, offset, count);
        next += count;
        return count;
    }
}
