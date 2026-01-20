package com.github.kjetilv.uplift.json.io;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.LongAdder;

public final class ByteChannelSink implements Sink {

    private final WritableByteChannel byteChannel;

    private final Charset charset;

    private final LongAdder bytesWritten = new LongAdder();

    public ByteChannelSink(WritableByteChannel byteChannel, Charset charset) {
        this.byteChannel = byteChannel;
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
    }

    @Override
    public Sink accept(String str) {
        try {
            var bytes = str.getBytes(charset);
            var byteBuffer = ByteBuffer.wrap(bytes);
            byteChannel.write(byteBuffer);
            bytesWritten.add(bytes.length);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(this + " failed to write `" + str + "`", e);
        }
    }

    @Override
    public Mark mark() {
        var l = bytesWritten.longValue();
        return () -> l != bytesWritten.longValue();
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bytesWritten + "->" + byteChannel + "]";
    }
}
