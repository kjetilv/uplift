package com.github.kjetilv.uplift.asynchttp.rere;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

final class BodyBytes implements ReadableByteChannel {

    private final ReadableByteChannel channel;

    private final ByteBuffer byteBuffer;

    BodyBytes(ReadableByteChannel channel, ByteBuffer byteBuffer) {
        this.channel = channel;
        this.byteBuffer = byteBuffer;
    }

    @Override
    public int read(ByteBuffer dst) {
        if (byteBuffer.hasRemaining()) {
            int toWrite = Math.min(dst.remaining(), byteBuffer.remaining());
            if (toWrite > 0) {
                int oldLimit = byteBuffer.limit();
                byteBuffer.limit(byteBuffer.position() + toWrite);
                dst.put(byteBuffer);
                byteBuffer.limit(oldLimit);
                return toWrite;
            }
        }
        try {
            return this.channel.read(dst);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read from " + this.channel, e);
        }
    }

    @Override
    public boolean isOpen() {
        return byteBuffer.hasRemaining() || this.channel.isOpen();
    }

    @Override
    public void close() {
        try {
            this.channel.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close " + this.channel, e);
        }
    }

}
