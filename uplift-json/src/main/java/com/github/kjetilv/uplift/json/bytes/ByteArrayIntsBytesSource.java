package com.github.kjetilv.uplift.json.bytes;

import module java.base;

public final class ByteArrayIntsBytesSource extends AbstractIntsBytesSource {

    private final byte[] bytes;

    private int i;

    private final int len;

    public ByteArrayIntsBytesSource(byte[] bytes) {
        this.bytes = Objects.requireNonNull(bytes, "bytes");
        this.len = this.bytes.length;
        this.initialize();
    }

    @Override
    protected byte nextByte() {
        return i == len ? 0 : bytes[i++];
    }
}
