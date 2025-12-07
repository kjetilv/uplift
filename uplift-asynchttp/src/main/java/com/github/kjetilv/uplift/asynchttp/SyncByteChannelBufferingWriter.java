package com.github.kjetilv.uplift.asynchttp;

import module java.base;

import static java.util.Objects.requireNonNull;

record SyncByteChannelBufferingWriter(WritableByteChannel channel) implements BufferingWriter<ByteBuffer> {

    SyncByteChannelBufferingWriter {
        requireNonNull(channel, "channel");
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to close " + channel, e);
        }
    }

    @Override
    public void write(Writable<? extends ByteBuffer> writable) {
        try {
            channel.write(writable.buffer());
        } catch (Exception e) {
            throw new IllegalStateException("Could not write to " + channel, e);
        }
    }
}
