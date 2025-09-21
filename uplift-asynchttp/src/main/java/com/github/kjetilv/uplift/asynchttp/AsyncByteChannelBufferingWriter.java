package com.github.kjetilv.uplift.asynchttp;

import module java.base;

import static java.util.Objects.requireNonNull;

record AsyncByteChannelBufferingWriter(AsynchronousByteChannel channel) implements BufferingWriter<ByteBuffer> {

    AsyncByteChannelBufferingWriter {
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
            channel.write(
                writable.buffer(),
                null,
                new WriteableHandler(writable, channel, 100)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Could not write to " + channel, e);
        }
    }
}
