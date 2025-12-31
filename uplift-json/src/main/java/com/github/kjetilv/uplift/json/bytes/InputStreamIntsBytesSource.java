package com.github.kjetilv.uplift.json.bytes;

import module java.base;

public final class InputStreamIntsBytesSource extends AbstractIntsBytesSource {

    private final InputStream stream;

    private final byte[] buffer;

    private int index;

    private int read;

    public InputStreamIntsBytesSource(InputStream stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
        this.buffer = new byte[1024];
        super();
    }

    @Override
    protected byte nextByte() {
        if (index == read) {
            index = 0;
            try {
                read = stream.read(buffer);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read from " + stream, e);
            }
            if (read < 0) {
                return -1;
            }
        }
        return buffer[index++];
    }
}
