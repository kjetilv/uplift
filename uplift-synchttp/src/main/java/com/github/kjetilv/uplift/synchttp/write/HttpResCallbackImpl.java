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

    private long contentLength = -1;

    private boolean done;

    HttpResCallbackImpl(WritableByteChannel out, ByteBuffer buffer) {
        this.out = Objects.requireNonNull(out, "out");
        this.buffer = Objects.requireNonNull(buffer, "buffer");
    }

    @Override
    public Headers status(int statusCode) {
        try {
            buffer(ByteBuffer.wrap(VERSION));
            buffer(statusCode(statusCode));
            buffer(ByteBuffer.wrap(CRLF));
            return this;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to respond " + statusCode, e);
        }
    }

    @Override
    public Headers header(String name, Object value) {
        buffer(ByteBuffer.wrap(name.getBytes()));
        buffer(ByteBuffer.wrap(COLON));
        buffer(ByteBuffer.wrap(value.toString().getBytes()));
        buffer(ByteBuffer.wrap(CRLF));
        return this;
    }

    @Override
    public Headers headers(String... literalHeaders) {
        var byteBuffer = ByteBuffer.wrap(sanitize(literalHeaders).getBytes());
        buffer(byteBuffer);
        return this;
    }

    @Override
    public Headers contentType(String contentType) {
        return header("content-type", contentType);
    }

    @Override
    public Body contentLength(long contentLength) {
        this.contentLength = contentLength;
        header("content-length", String.valueOf(this.contentLength));
        return this;
    }

    @Override
    public void nobody() {
        failDone();
        try {
            contentLength(0L);
            flushHeaders();
        } finally {
            done = true;
        }
    }

    @Override
    public void body(String content) {
        failDone();
        body(content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void body(byte[] bytes) {
        failDone();
        try {
            flushHeaders();
            if (bytes == null || bytes.length == 0) {
                checkZeroLength();
            } else {
                checkDeclaredLength();
                var written = stream(ByteBuffer.wrap(bytes));
                checkWrittenLength(written);
            }
        } finally {
            done = true;
        }
    }

    @Override
    public void body(ReadableByteChannel channel) {
        failDone();
        try {
            flushHeaders();
            checkDeclaredLength();
            var written = transferFrom(channel);
            checkWrittenLength(written);
        } finally {
            done = true;
        }
    }

    @Override
    public void channel(Consumer<WritableByteChannel> channelWriter) {
        failDone();
        try {
            if (contentLength == -1) {
                header("transfer-encoding", "chunked");
            }
            flushHeaders();
            channelWriter.accept(out);
        } finally {
            done = true;
        }
    }

    private void failDone() {
        if (done) {
            throw new IllegalStateException("Already done");
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

    private void checkWrittenLength(int written) {
        if (written != contentLength) {
            throw new IllegalStateException(
                "Expected " + contentLength + " bytes, but wrote " + written);
        }
    }

    private int transferFrom(ReadableByteChannel body) {
        int written = 0;
        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
        while (Utils.didRead(body, buffer)) {
            written += stream(buffer);
        }
        return written;
    }

    private void flushHeaders() {
        buffer(ByteBuffer.wrap(CRLF));
        buffer.flip();
        stream(buffer);
    }

    private void buffer(ByteBuffer delta) {
        failDone();
        try {
            this.buffer.put(delta);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write headers > buffer size: " + buffer.capacity(), e);
        }
    }

    private int stream(ByteBuffer delta) {
        try {
            int written = 0;
            while (delta.hasRemaining()) {
                written += out.write(delta);
            }
            return written;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to write " + delta, e);
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
        return ByteBuffer.wrap(String.valueOf(code).getBytes());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               out + (contentLength >= 0 ? " bytes:" + contentLength : "") +
               "]";
    }
}
