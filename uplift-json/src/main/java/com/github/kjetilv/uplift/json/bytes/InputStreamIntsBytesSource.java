package com.github.kjetilv.uplift.json.bytes;

import module java.base;

/// Source over an input stream, read in chunks
public final class InputStreamIntsBytesSource extends AbstractIntsBytesSource {

    private final InputStream stream;

    public InputStreamIntsBytesSource(InputStream stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
        super();
    }

    @Override
    protected int read(byte[] dst, int offset, int length) {
        try {
            return stream.read(dst, offset, length);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + stream, e);
        }
    }
}
