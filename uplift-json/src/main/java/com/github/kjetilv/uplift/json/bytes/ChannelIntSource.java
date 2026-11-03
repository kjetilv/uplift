package com.github.kjetilv.uplift.json.bytes;

import module java.base;

/// Source over the next `len` bytes of a channel, read in chunks. Never reads past `len` bytes.
public class ChannelIntSource extends AbstractIntsBytesSource {

    private final ReadableByteChannel channel;

    private final long len;

    private long consumed;

    public ChannelIntSource(ReadableByteChannel channel, long len) {
        this.channel = Objects.requireNonNull(channel, "channel");
        if (len < 0) {
            throw new IllegalArgumentException("len must be >= 0: " + len);
        }
        this.len = len;
        super();
    }

    @Override
    protected int read(byte[] dst, int offset, int length) {
        var remaining = len - consumed;
        if (remaining <= 0) {
            return -1;
        }
        int read;
        try {
            read = channel.read(ByteBuffer.wrap(dst, offset, (int) Math.min(length, remaining)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + channel, e);
        }
        if (read > 0) {
            consumed += read;
        }
        return read;
    }
}
