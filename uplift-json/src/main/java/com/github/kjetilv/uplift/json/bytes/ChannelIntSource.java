package com.github.kjetilv.uplift.json.bytes;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

public class ChannelIntSource extends AbstractIntsBytesSource {

    private final ReadableByteChannel channel;

    private final int len;

    private int i;

    public ChannelIntSource(
        ReadableByteChannel channel,
        int len
    ) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.len = len;
    }

    @Override
    protected byte nextByte() {
        if (i++ == len) {
            return 0;
        }
        var allocate = ByteBuffer.allocate(1);
        int read;
        try {
            read = channel.read(allocate);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read from " + channel, e);
        }
        return read == 1
            ? allocate.get(0)
            : 0;
    }
}
