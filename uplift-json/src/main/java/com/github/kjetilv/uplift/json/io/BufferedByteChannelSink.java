package com.github.kjetilv.uplift.json.io;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.LongAdder;

public final class BufferedByteChannelSink implements Sink {

    private final WritableByteChannel byteChannel;

    private final Charset charset;

    private final LongAdder bytesWritten = new LongAdder();

    private final int bufferSize;

    private final ByteBuffer buffer;

    public BufferedByteChannelSink(WritableByteChannel byteChannel, Charset charset, int bufferSize) {
        this.byteChannel = byteChannel;
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
        this.bufferSize = bufferSize;
        this.buffer = ByteBuffer.allocateDirect(this.bufferSize);
    }

    @Override
    public Sink accept(String str) {
        var bytes = str.getBytes(charset);
        if (bytes.length > bufferSize) {
            bytesWritten.add(
                flush(buffer) +
                flush(ByteBuffer.wrap(bytes))
            );
        } else if (bytes.length > buffer.remaining()) {
            var written = flush(buffer);
            buffer.put(bytes);
            bytesWritten.add(written);
        } else {
            buffer.put(bytes);
        }
        return this;
    }

    @Override
    public long length() {
        return bytesWritten.longValue() + buffer.position();
    }

    @Override
    public void close() {
        flush(buffer);
    }

    private int flush(ByteBuffer buffer) {
        buffer.flip();
        int written = 0;
        try {
            while (buffer.hasRemaining()) {
                written += byteChannel.write(buffer);
            }
            return written;
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to flush buffer", e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bytesWritten + "->" + byteChannel + "]";
    }
}
