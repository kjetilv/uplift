package com.github.kjetilv.uplift.json.bytes;

import module java.base;

public final class InputStreamIntsBytesSource extends AbstractIntsBytesSource {

    private final InputStream stream;

    private final byte[] buffer;

    private int index;

    private int read;

    private boolean ended;

    public InputStreamIntsBytesSource(InputStream stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
        this.buffer = new byte[1024];
        super();
    }

    @Override
    /// @return Next byte, or 0 at the end of the stream, and on every call after that
    protected byte nextByte() {
        if (ended) {
            return 0;
        }
        if (index == read) {
            index = 0;
            try {
                read = stream.read(buffer);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read from " + stream, e);
            }
            if (read < 0) {
                ended = true;
                return 0;
            }
        }
        return buffer[index++];
    }
}
