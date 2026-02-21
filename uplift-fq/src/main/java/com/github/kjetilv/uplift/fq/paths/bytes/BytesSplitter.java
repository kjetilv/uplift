package com.github.kjetilv.uplift.fq.paths.bytes;

import module java.base;

public final class BytesSplitter {

    private final byte[] buffer;

    private final int limiter;

    private final InputStream inputStream;

    private int index;

    private int end;

    private boolean exhausted;

    public BytesSplitter(InputStream inputStream, int limiter, int size) {
        this.buffer = new byte[size > 0 ? size : DEFAULT_BUFFER_SIZE];
        this.limiter = limiter;
        this.inputStream = inputStream;
    }

    public byte[] next() {
        var limit = index;
        while (true) {
            if (index == end) {
                load(limit);
                if (index == end && exhausted) {
                    return null;
                }
            }
            var next = buffer[index];
            try {
                if (next == limiter) {
                    return segment(limit);
                }
            } finally {
                nextIndex();
            }
        }
    }

    private void nextIndex() {
        index = (index + 1) % buffer.length;
    }

    private byte[] segment(int limit) {
        if (index == limit) {
            return EMPTY;
        }
        if (index > limit) {
            var segment = new byte[index - limit];
            System.arraycopy(buffer, limit, segment, 0, segment.length);
            return segment;
        }
        if (index > 0) {
            var tail = buffer.length - limit;
            var segment = new byte[tail + index];
            System.arraycopy(buffer, limit, segment, 0, tail);
            System.arraycopy(buffer, 0, segment, tail, index);
            return segment;
        }
        var tail = buffer.length - limit;
        var segment = new byte[tail];
        System.arraycopy(buffer, limit, segment, 0, tail);
        return segment;
    }

    /// Loads data into the buffer.
    private void load(int limit) {
        if (exhausted) {
            return;
        }
        if (limit == 0 && index == 0) {
            // Fresh start! Fill the buffer as far as we can
            var loaded = load(0, buffer.length);
            if (loaded == -1) {
                exhausted = true;
            } else {
                end = loaded % buffer.length;
            }
        } else if (limit > 0 && index == 0) {
            // Wrapping around the buffer: Load head up to the limit
            var loaded = load(0, limit);
            if (loaded == -1) {
                exhausted = true;
            } else {
                end = loaded % buffer.length;
            }
        } else if (index > 0 && limit > index) {
            // Need to fill inside the buffer
            var loaded = load(index, limit - index);
            if (loaded == -1) {
                exhausted = true;
            } else {
                end += loaded;
            }
        } else {
            // Need to fill the tail and then the head
            var loaded = load(index, buffer.length - index);
            if (loaded == -1) {
                exhausted = true;
            } else {
                end = loaded < buffer.length - index
                    ? end + loaded
                    : 0;
            }
        }
    }

    private int load(int off, int len) {
        try {
            return inputStream.read(buffer, off, len);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + off + "+" + len, e);
        }
    }

    private static final byte[] EMPTY = new byte[0];

    private static final int DEFAULT_BUFFER_SIZE = 8192;
}
