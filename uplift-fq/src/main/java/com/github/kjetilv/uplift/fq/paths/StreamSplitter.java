package com.github.kjetilv.uplift.fq.paths;

import java.io.InputStream;

public final class StreamSplitter {

    private final byte[] buffer;

    private final int limiter;

    private final InputStream inputStream;

    public int index;

    public int end;

    private boolean exhausted;

    public StreamSplitter(InputStream inputStream, int limiter, int size) {
        this.buffer = new byte[size > 0 ? size : DEFAULT_BUFFER_SIZE];
        this.limiter = limiter;
        this.inputStream = inputStream;
    }

    public byte[] next() {
        int startIndex = index;
        while (true) {
            if (index == end) {
                load(startIndex);
                if (index == end && exhausted) {
                    return null;
                }
            }
            byte next = buffer[index];
            try {
                if (next == limiter) {
                    return segment(startIndex);
                }
            } finally {
                nextIndex();
            }
        }
    }

    private void nextIndex() {
        index = (index + 1) % buffer.length;
    }

    private byte[] segment(int startIndex) {
        if (index > startIndex) {
            var segment = new byte[index - startIndex];
            System.arraycopy(buffer, startIndex, segment, 0, segment.length);
            return segment;
        }
        var tail = buffer.length - startIndex;
        byte[] segment = new byte[tail + index];
        System.arraycopy(buffer, startIndex, segment, 0, tail);
        System.arraycopy(buffer, 0, segment, tail, index);
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
            end += loaded;
        } else  {
            // Need to fill the tail and then the head
            var tail = buffer.length - index;
            var tailLoad = load(index, tail);
            if (tailLoad == -1) {
                exhausted = true;
            } else {
                end = tailLoad < tail
                    ? end + tailLoad
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

    public static final int DEFAULT_BUFFER_SIZE = 8192;
}
