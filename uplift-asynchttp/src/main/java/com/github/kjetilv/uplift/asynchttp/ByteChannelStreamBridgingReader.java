package com.github.kjetilv.uplift.asynchttp;

import module java.base;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public record ByteChannelStreamBridgingReader(
    InputStream in,
    ByteBuffer buffer
) implements BufferedReader<ByteBuffer> {

    public ByteChannelStreamBridgingReader(InputStream in, ByteBuffer buffer) {
        this.in = requireNonNull(in, "in");
        this.buffer = requireNonNull(buffer, "buffer");
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to close " + in, e);
        }
    }

    @Override
    public ByteBuffer buffer(int size) {
        return buffer;
    }

    @Override
    public int read(ByteBuffer buffer) {
        try {
            buffer.clear();
            int read = in.read(buffer.array());
            if (read > 0) {
                buffer.limit(read);
            }
            return read;
        } catch (Exception e) {
            throw new IllegalStateException("Could not read from " + in, e);
        }
    }
}
