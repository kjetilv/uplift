package com.github.kjetilv.uplift.synchttp.write;

import com.github.kjetilv.uplift.synchttp.Utils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class HttpResCallbackImpl implements
    HttpResponseCallback,
    HttpResponseCallback.Headers,
    HttpResponseCallback.Body {

    private final WritableByteChannel out;

    private final ByteBuffer buffer;

    private boolean headersComplete;

    private long contentLength = -1;

    HttpResCallbackImpl(WritableByteChannel out, ByteBuffer buffer) {
        this.out = Objects.requireNonNull(out, "out");
        this.buffer = Objects.requireNonNull(buffer, "buffer");
    }

    @Override
    public Headers status(int statusCode) {
        try {
            write(ByteBuffer.wrap(VERSION));
            write(statusCode(statusCode));
            return this;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to respond " + statusCode, e);
        }
    }

    @Override
    public Headers header(String name, Object value) {
        write(ByteBuffer.wrap(name.getBytes()));
        write(ByteBuffer.wrap(COLON));
        write(ByteBuffer.wrap(value.toString().getBytes()));
        write(ByteBuffer.wrap(CRLF));
        return this;
    }

    @Override
    public Headers headers(String... literalHeaders) {
        return writeHeaders(sanitize(literalHeaders));
    }

    @Override
    public Headers contentType(String contentType) {
        return header("content-type", contentType);
    }

    @Override
    public Body contentLength(long contentLength) {
        this.contentLength = contentLength;
        if (this.contentLength > 0) {
            header("content-length", String.valueOf(this.contentLength));
        }
        return content();
    }

    @Override
    public void nobody() {
        write(ByteBuffer.wrap(LN));
        flushHeaders();
    }

    @Override
    public Body content() {
        write(ByteBuffer.wrap(LN));
        return flushHeaders();
    }

    @Override
    public void body(String content) {
        flushHeaders();
        checkDeclaredLength();
        if (content != null && !content.isEmpty()) {
            int written = write(ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8)));
            checkWrittenLength(written);
        }
    }

    @Override
    public void body(byte[] bytes) {
        flushHeaders();
        checkDeclaredLength();
        if (bytes != null && bytes.length != 0) {
            int written = write(ByteBuffer.wrap(bytes));
            checkWrittenLength(written);
        }
    }

    @Override
    public void body(ReadableByteChannel channel) {
        flushHeaders();
        checkDeclaredLength();
        var written = writeBody(channel);
        checkWrittenLength(written);
    }

    @Override
    public void channel(Consumer<WritableByteChannel> channelWriter) {
        if (contentLength == -1) {
            header("transfer-encoding", "chunked");
        }
        write(ByteBuffer.wrap(LN));
        flushHeaders();
        channelWriter.accept(out);
    }

    private void checkDeclaredLength() {
        if (contentLength == -1) {
            throw new IllegalStateException("No content length provided");
        }
    }

    private void checkWrittenLength(int written) {
        if (written != contentLength) {
            throw new IllegalStateException(
                "Expected " + contentLength + " bytes, but wrote " + written);
        }
    }

    private HttpResCallbackImpl writeHeaders(String literalHeaders) {
        var byteBuffer = ByteBuffer.wrap(literalHeaders.getBytes());
        write(byteBuffer);
        return this;
    }

    private int writeBody(ReadableByteChannel body) {
        int written = 0;
        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
        while (Utils.readInto(body, buffer)) {
            written += write(buffer);
            buffer.position(0);
        }
        return written;
    }

    private Body flushHeaders() {
        if (!headersComplete) {
            headersComplete = true;
            buffer.flip();
            write(buffer);
        }
        return this;
    }

    private int write(ByteBuffer delta) {
        if (headersComplete) {
            try {
                int written = 0;
                while (delta.hasRemaining()) {
                    written += out.write(delta);
                }
                return written;
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to write " + delta, e);
            }
        } else {
            try {
                this.buffer.put(delta);
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Failed to write headers, exceeded buffer size: " + buffer.capacity(),
                    e
                );
            }
            return delta.position();
        }
    }

    private static final byte[] VERSION = "HTTP/1.1 ".getBytes();

    private static final byte[] LN = {'\r', '\n'};

    private static final byte[] COLON = ": ".getBytes();

    private static final byte[] CRLF = "\r\n".getBytes();

    private static String sanitize(String... literalHeaders) {
        return Arrays.stream(literalHeaders)
            .map(header -> header.split("\n"))
            .flatMap(Arrays::stream)
            .map(String::trim)
            .flatMap(line ->
                Stream.of(line, "\r\n"))
            .collect(Collectors.joining());
    }

    private static ByteBuffer statusCode(int code) {
        return ByteBuffer.wrap((code + "\r\n").getBytes());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               out + (contentLength >= 0 ? " bytes:" + contentLength : "") +
               "]";
    }
}
