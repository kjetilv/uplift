package com.github.kjetilv.uplift.synchttp.write;

import module java.base;
import com.github.kjetilv.uplift.synchttp.Utils;
import com.github.kjetilv.uplift.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class HttpResCallbackImpl implements
    HttpResponseCallback,
    HttpResponseCallback.Headers,
    HttpResponseCallback.Body {

    private static final Logger log = LoggerFactory.getLogger(HttpResCallbackImpl.class);

    private final WritableByteChannel out;

    private final ByteBuffer buffer;

    private final boolean closeConnection;

    private boolean connectionHeader;

    private long contentLength = -1;

    private boolean started;

    private boolean committed;

    private boolean done;

    private boolean failed;

    /// @param out             Connection
    /// @param buffer          Buffer for the status line and headers
    /// @param closeConnection True if the server will close the connection after this response. A
    ///                        `connection: close` header is added, unless the handler sets a connection header.
    HttpResCallbackImpl(WritableByteChannel out, ByteBuffer buffer, boolean closeConnection) {
        this.out = Objects.requireNonNull(out, "out");
        this.buffer = Objects.requireNonNull(buffer, "buffer");
        this.closeConnection = closeConnection;
    }

    @Override
    public Headers status(int statusCode) {
        if (started) {
            throw new IllegalStateException("Status already set");
        }
        started = true;
        buffer(ByteBuffer.wrap(StatusLine.bytes(statusCode)));
        return this;
    }

    @Override
    public boolean committed() {
        return committed;
    }

    @Override
    public boolean finish() {
        if (started && !done && contentLength == -1) {
            nobody();
        }
        return done && !failed;
    }

    @Override
    public Headers header(String name, Object value) {
        if (name.equalsIgnoreCase(CONNECTION)) {
            connectionHeader = true;
        }
        put(name);
        put(COLON);
        put(value.toString());
        put(CRLF);
        return this;
    }

    @Override
    public Headers headers(String... literalHeaders) {
        var lines = lines(literalHeaders);
        if (lines.stream().anyMatch(HttpResCallbackImpl::isConnectionHeader)) {
            connectionHeader = true;
        }
        buffer(ByteBuffer.wrap(lines.stream()
            .map(line -> line + "\r\n")
            .collect(Collectors.joining())
            .getBytes()));
        return this;
    }

    @Override
    public Headers contentType(String contentType) {
        return header("content-type", contentType);
    }

    @Override
    public Body contentLength(long contentLength) {
        this.contentLength = contentLength;
        put(CONTENT_LENGTH);
        putDigits(contentLength);
        put(CRLF);
        return this;
    }

    @Override
    public void nobody() {
        complete(() -> {
            contentLength(0L);
            flushHeaders();
        });
    }

    @Override
    public void body(String content) {
        complete(() ->
            body(content.getBytes(StandardCharsets.UTF_8))
        );
    }

    @Override
    public void body(byte[] bytes) {
        complete(() -> {
            if (bytes == null || bytes.length == 0) {
                checkZeroLength();
                flushHeaders();
            } else {
                checkDeclaredLength(bytes.length);
                endHeaders();
                if (bytes.length <= buffer.remaining()) {
                    var headerLength = buffer.position();
                    buffer.put(bytes);
                    var written = stream(buffer.flip());
                    checkWrittenLength(written.minus(headerLength));
                } else {
                    var headerLength = buffer.flip().remaining();
                    var written = stream(buffer, ByteBuffer.wrap(bytes));
                    checkWrittenLength(written.minus(headerLength));
                }
            }
        });
    }

    @Override
    public void body(ReadableByteChannel channel) {
        complete(() -> {
            checkDeclaredLength();
            flushHeaders();
            var written = transferFrom(channel);
            checkWrittenLength(written);
        });
    }

    @Override
    public void channel(Consumer<WritableByteChannel> channelWriter) {
        complete(() -> {
            if (contentLength == -1) {
                header("transfer-encoding", "chunked");
            }
            flushHeaders();
            channelWriter.accept(out);

        });
    }

    private void complete(Runnable action) {
        if (done) {
            throw new IllegalStateException("Already done");
        }
        try {
            action.run();
        } catch (RuntimeException e) {
            failed = true;
            throw e;
        } finally {
            done = true;
        }
    }

    private void checkZeroLength() {
        if (contentLength > 0) {
            throw new IllegalStateException("No content provided, but content-length is " + contentLength);
        }
    }

    private void checkDeclaredLength() {
        if (contentLength <= 0) {
            throw new IllegalStateException("No content length provided");
        }
    }

    private void checkDeclaredLength(int length) {
        if (contentLength != length) {
            throw new IllegalStateException("Content-length is " + contentLength + ", but body is " + length + " bytes");
        }
    }

    private void checkWrittenLength(WriteResult written) {
        if (written.terminated()) {
            log.warn("Wrote {} bytes (terminated)", written.count());
        } else if (written.count() != contentLength) {
            throw new IllegalStateException("Expected " + contentLength + " bytes, but wrote " + written);
        }
    }

    /// Copies the body through the header buffer, which is free once the headers are written
    private WriteResult transferFrom(ReadableByteChannel body) {
        var written = new WriteResult();
        buffer.clear();
        while (!written.terminated() && Utils.didRead(body, buffer)) {
            written = written.add(stream(buffer));
            buffer.clear();
        }
        return written;
    }

    private void flushHeaders() {
        endHeaders();
        stream(buffer.flip());
    }

    /// Adds the last header lines and the empty line to the buffer
    private void endHeaders() {
        if (closeConnection && !connectionHeader) {
            header(CONNECTION, "close");
        }
        put(CRLF);
        committed = true;
    }

    /// Adds a string to the header buffer. ASCII is copied directly, anything else is encoded as UTF-8.
    private void put(String string) {
        var length = string.length();
        if (length <= buffer.remaining()) {
            if (done) {
                throw new IllegalStateException("Already done");
            }
            var start = buffer.position();
            for (var i = 0; i < length; i++) {
                var c = string.charAt(i);
                if (c >= 0x80) {
                    buffer.position(start);
                    buffer(ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8)));
                    return;
                }
                buffer.put((byte) c);
            }
        } else {
            buffer(ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void putDigits(long value) {
        if (value < 0 || MAX_DIGITS > buffer.remaining()) {
            put(Long.toString(value));
            return;
        }
        var digits = 1;
        for (var rest = value / 10; rest > 0; rest /= 10) {
            digits++;
        }
        var position = buffer.position();
        var rest = value;
        for (var i = digits - 1; i >= 0; i--) {
            buffer.put(position + i, (byte) ('0' + rest % 10));
            rest /= 10;
        }
        buffer.position(position + digits);
    }

    /// Adds to the header buffer. If the buffer is full, it is written to the connection first, so the
    /// header section has no size limit.
    private void buffer(ByteBuffer delta) {
        if (done) {
            throw new IllegalStateException("Already done");
        }
        if (delta.remaining() > buffer.remaining()) {
            buffer.flip();
            committed = true;
            stream(buffer);
            buffer.clear();
            if (delta.remaining() > buffer.remaining()) {
                stream(delta);
                return;
            }
        }
        buffer.put(delta);
    }

    private WriteResult stream(ByteBuffer buffer) {
        var written = 0L;
        try {
            while (buffer.hasRemaining()) {
                written += out.write(buffer);
            }
            return new WriteResult(written, false);
        } catch (Exception e) {
            return failed(e, written, buffer);
        }
    }

    /// Writes the buffers in order, with a gathering write if the channel supports it
    private WriteResult stream(ByteBuffer first, ByteBuffer second) {
        var written = 0L;
        try {
            if (out instanceof GatheringByteChannel gathering) {
                var buffers = new ByteBuffer[] {first, second};
                while (second.hasRemaining()) {
                    written += gathering.write(buffers);
                }
            } else {
                written += stream(first).count();
                written += stream(second).count();
            }
            return new WriteResult(written, false);
        } catch (Exception e) {
            return failed(e, written, second);
        }
    }

    private static WriteResult failed(Exception e, long written, ByteBuffer buffer) {
        if (Throwables.clientFailure(e)) {
            log.warn("Failed to write {}, connection was closed: {}", buffer, Throwables.summary(e));
            return new WriteResult(written, true);
        }
        throw new IllegalArgumentException("Failed to write " + buffer, e);
    }

    private static final String COLON = ": ";

    private static final String CRLF = "\r\n";

    private static final String CONTENT_LENGTH = "content-length: ";

    /// Digits in Long.MAX_VALUE
    private static final int MAX_DIGITS = 19;

    private static final String CONNECTION = "connection";

    private static boolean isConnectionHeader(String line) {
        return line.regionMatches(true, 0, CONNECTION + ":", 0, CONNECTION.length() + 1);
    }

    /// @return Header lines, without line breaks and blank lines. A blank line would end the header section.
    private static List<String> lines(String... literalHeaders) {
        return Arrays.stream(literalHeaders)
            .map(header -> header.split("\n"))
            .flatMap(Arrays::stream)
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .toList();
    }

    private record WriteResult(long count, boolean terminated) {

        private WriteResult() {
            this(0, false);
        }

        WriteResult add(WriteResult stream) {
            return terminated
                ? this
                : new WriteResult(count + stream.count(), stream.terminated());
        }

        WriteResult minus(long bytes) {
            return new WriteResult(count - bytes, terminated);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               out + (contentLength >= 0 ? " bytes:" + contentLength : "") +
               "]";
    }
}
