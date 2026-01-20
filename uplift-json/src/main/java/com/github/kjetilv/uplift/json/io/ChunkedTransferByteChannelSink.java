package com.github.kjetilv.uplift.json.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.LongAdder;

import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class ChunkedTransferByteChannelSink implements Sink {

    private final WritableByteChannel byteChannel;

    private final Charset charset;

    private final LongAdder bytesWritten = new LongAdder();

    private final int bufferSize;

    private final ByteBuffer buffer;

    private final int maxHeader;

    public ChunkedTransferByteChannelSink(WritableByteChannel byteChannel, Charset charset, int bufferSize) {
        this.byteChannel = byteChannel;
        this.charset = charset == null ? UTF_8 : charset;
        this.maxHeader = Integer.numberOfTrailingZeros(Integer.highestOneBit(bufferSize));
        this.bufferSize = bufferSize;
        this.buffer = ByteBuffer.allocateDirect(maxHeader + CRLF.length + this.bufferSize + CRLF.length);
        this.buffer.position(maxHeader);
    }

    @Override
    public Sink accept(String str) {
        var bytes = str.getBytes(charset);
        if (bytes.length > bufferSize) {
            bytesWritten.add(
                flush(buffer) +
                flush(ByteBuffer.wrap(bytes))
            );
        } else if (bytes.length > buffer.remaining() - CRLF.length) {
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
        if (buffer.position() > maxHeader) {
            flush(buffer);
        }
        write(ByteBuffer.wrap(TAIL));
    }

    private int flush(ByteBuffer buffer) {
        chunk(header(buffer), buffer);
        var written = 0;
        try {
            while (buffer.hasRemaining()) {
                written += write(buffer);
            }
            return written;
        } finally {
            buffer.clear();
            buffer.position(maxHeader);
        }
    }

    private int write(ByteBuffer buffer) {
        try {
            return byteChannel.write(buffer);
        } catch (IOException e) {
            throw new IllegalStateException(this + " failed to write " + buffer, e);
        }
    }

    private byte[] header(ByteBuffer buffer) {
        var chunkSize = buffer.position() - maxHeader;
        byte[] countBytes = Integer.toHexString(chunkSize).getBytes(UTF_8);
        byte[] header = new byte[countBytes.length + CRLF.length];
        arraycopy(countBytes, 0, header, 0, countBytes.length);
        arraycopy(CRLF, 0, header, countBytes.length, CRLF.length);
        return header;
    }

    private void chunk(byte[] header, ByteBuffer buffer) {
        var index = maxHeader - header.length;
        buffer.put(ByteBuffer.wrap(CRLF))
            .put(
                index,
                header,
                0,
                header.length
            )
            .flip()
            .position(index);
    }

    private static final String S = "\r\n";

    private static final byte[] CRLF = S.getBytes(UTF_8);

    private static final byte[] TAIL = ("0" + S + S).getBytes(UTF_8);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bytesWritten + "->" + byteChannel + "]";
    }
}
