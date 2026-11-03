package com.github.kjetilv.uplift.json.bytes;

/// Source over a byte array, scanned in place
public final class ByteArrayIntsBytesSource extends AbstractIntsBytesSource {

    public ByteArrayIntsBytesSource(byte[] bytes) {
        super(bytes, 0, bytes.length);
    }

    public ByteArrayIntsBytesSource(byte[] bytes, int offset, int length) {
        super(bytes, offset, length);
    }
}
