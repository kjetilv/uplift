package com.github.kjetilv.uplift.json.io;

import module java.base;

/// Writes to a channel through a buffer. The buffer is written when the next write does not fit, and on
/// [#close()]. A write larger than the buffer goes straight to the channel, after the buffer.
public final class BufferedByteChannelSink extends AbstractBufferedSink {

    private final WritableByteChannel byteChannel;

    private final ByteBuffer buffer;

    private long length;

    public BufferedByteChannelSink(
        WritableByteChannel byteChannel,
        Charset charset,
        int bufferSize
    ) {
        super(charset, bufferSize);
        this.byteChannel = Objects.requireNonNull(byteChannel, "byteChannel");
        this.buffer = ByteBuffer.allocateDirect(bufferSize());
    }

    @Override
    public void close() {
        flush();
    }

    @Override
    public void accept(String string) {
        var bytes = string.getBytes(charset());
        length += bytes.length;
        if (bytes.length > buffer.remaining()) {
            flush();
        }
        if (bytes.length > buffer.capacity()) {
            write(ByteBuffer.wrap(bytes));
        } else {
            buffer.put(bytes);
        }
    }

    /// @return Bytes accepted so far, written or buffered
    @Override
    public long length() {
        return length;
    }

    private void flush() {
        buffer.flip();
        try {
            write(buffer);
        } finally {
            buffer.clear();
        }
    }

    private void write(ByteBuffer source) {
        try {
            while (source.hasRemaining()) {
                byteChannel.write(source);
            }
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to write", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + length + "->" + byteChannel + "]";
    }
}
