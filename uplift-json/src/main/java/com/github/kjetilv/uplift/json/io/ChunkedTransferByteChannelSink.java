package com.github.kjetilv.uplift.json.io;

import module java.base;

import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;

/// Writes to a channel in HTTP chunked transfer coding. Each chunk is buffered, with space reserved in front
/// for its size line.
///
/// A write that does not fit in the current chunk starts a new one. A write larger than a chunk is split
/// over several. [#close()] writes the last chunk and the terminating empty chunk.
public final class ChunkedTransferByteChannelSink extends AbstractBufferedSink {

    private final WritableByteChannel byteChannel;

    private final ByteBuffer buffer;

    /// Space reserved for the size line: hex digits of the largest chunk, and CRLF
    private final int maxHeader;

    private long length;

    public ChunkedTransferByteChannelSink(WritableByteChannel byteChannel, Charset charset, int bufferSize) {
        super(charset, bufferSize);
        this.byteChannel = Objects.requireNonNull(byteChannel, "byteChannel");
        this.maxHeader = Integer.toHexString(maxChunk()).length() + CRLF.length;
        this.buffer = ByteBuffer.allocateDirect(maxHeader + maxChunk() + CRLF.length);
        this.buffer.position(maxHeader);
    }

    @Override
    public void accept(String string) {
        var bytes = string.getBytes(charset());
        length += bytes.length;
        if (bytes.length > available()) {
            flush();
        }
        var offset = 0;
        while (bytes.length - offset > available()) {
            var part = available();
            buffer.put(bytes, offset, part);
            offset += part;
            flush();
        }
        buffer.put(bytes, offset, bytes.length - offset);
    }

    /// @return Bytes accepted so far, written or buffered, not counting chunk framing
    @Override
    public long length() {
        return length;
    }

    @Override
    public void close() {
        flush();
        write(ByteBuffer.wrap(TAIL));
    }

    /// Room for chunk data. The buffer holds [#bufferSize()] plus the length of CRLF, as it always has.
    private int maxChunk() {
        return bufferSize() + CRLF.length;
    }

    private int available() {
        return buffer.limit() - CRLF.length - buffer.position();
    }

    /// Writes the buffered data as one chunk, unless it is empty: an empty chunk ends the stream.
    private void flush() {
        if (buffer.position() == maxHeader) {
            return;
        }
        chunk(header());
        try {
            while (buffer.hasRemaining()) {
                write(buffer);
            }
        } finally {
            buffer.clear();
            buffer.position(maxHeader);
        }
    }

    private void write(ByteBuffer source) {
        try {
            while (source.hasRemaining()) {
                byteChannel.write(source);
            }
        } catch (Exception e) {
            throw new IllegalStateException(this + " failed to write " + source, e);
        }
    }

    private byte[] header() {
        var chunkSize = buffer.position() - maxHeader;
        var countBytes = Integer.toHexString(chunkSize).getBytes(UTF_8);
        var header = new byte[countBytes.length + CRLF.length];
        arraycopy(countBytes, 0, header, 0, countBytes.length);
        arraycopy(CRLF, 0, header, countBytes.length, CRLF.length);
        return header;
    }

    private void chunk(byte[] header) {
        var index = maxHeader - header.length;
        buffer.put(CRLF)
            .put(index, header, 0, header.length)
            .flip()
            .position(index);
    }

    private static final String S = "\r\n";

    private static final byte[] CRLF = S.getBytes(UTF_8);

    private static final byte[] TAIL = ("0" + S + S).getBytes(UTF_8);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + length + "->" + byteChannel + "]";
    }
}
