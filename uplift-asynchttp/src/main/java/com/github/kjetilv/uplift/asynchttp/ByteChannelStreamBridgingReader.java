package com.github.kjetilv.uplift.asynchttp;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public final class ByteChannelStreamBridgingReader implements BufferedReader<ByteBuffer> {

    private final InputStream in;

    private final ByteBuffer buffer;

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
