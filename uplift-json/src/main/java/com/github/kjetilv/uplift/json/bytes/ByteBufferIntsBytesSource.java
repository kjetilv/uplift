package com.github.kjetilv.uplift.json.bytes;

import module java.base;

public final class ByteBufferIntsBytesSource extends AbstractIntsBytesSource {

    private final ByteBuffer bytes;

    private int i;

    private final int len;

    public ByteBufferIntsBytesSource(ByteBuffer bytes) {
        this.bytes = Objects.requireNonNull(bytes, "bytes");
        this.len = bytes.capacity();
        super();
    }

    @Override
    protected byte nextByte() {
        return i == len ? 0 : bytes.get(i++);
    }
}
