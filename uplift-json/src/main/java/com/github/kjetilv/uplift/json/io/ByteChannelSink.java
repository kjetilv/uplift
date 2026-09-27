package com.github.kjetilv.uplift.json.io;

import module java.base;

public final class ByteChannelSink extends AbstractEncodingSink {

    private final WritableByteChannel byteChannel;

    private final LongAdder bytesWritten = new LongAdder();

    public ByteChannelSink(WritableByteChannel byteChannel, Charset charset) {
        super(charset);
        this.byteChannel = byteChannel;
    }

    @Override
    public void accept(String string) {
        try {
            var bytes = string.getBytes(charset());
            var byteBuffer = ByteBuffer.wrap(bytes);
            byteChannel.write(byteBuffer);
            bytesWritten.add(bytes.length);
        } catch (Exception e) {
            throw new RuntimeException(this + " failed to write `" + string + "`", e);
        }
    }

    @Override
    public Mark mark() {
        var l = bytesWritten.longValue();
        return () -> l != bytesWritten.longValue();
    }

    @Override
    public long length() {
        return bytesWritten.longValue();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bytesWritten + "->" + byteChannel + "]";
    }
}
