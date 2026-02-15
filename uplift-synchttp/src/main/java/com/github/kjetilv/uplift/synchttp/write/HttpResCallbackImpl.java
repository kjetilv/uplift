package com.github.kjetilv.uplift.synchttp.write;

import com.github.kjetilv.uplift.synchttp.HttpMethod;
import com.github.kjetilv.uplift.synchttp.Utils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
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

    private long contentLength = -1;

    HttpResCallbackImpl(WritableByteChannel out) {
        this.out = Objects.requireNonNull(out, "out");
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
        return writeHeaders("%s: %s\r\n".formatted(name, value));
    }

    @Override
    public Headers headers(String... literalHeaders) {
        return writeHeaders(sanitize(literalHeaders));
    }

    @Override
    public Headers contentType(String contentType) {
        return writeHeaders("""
            content-type: %s\r
            """.formatted(contentType));
    }

    @Override
    public Headers cors(String host, HttpMethod... methods) {
        return writeHeaders("""
            access-control-allow-origin: %s\r
            access-control-allow-methods: %s\r
            """.formatted(
            host == null || host.isBlank()
                ? "*"
                : host,
            Stream.of(methods)
                .filter(Objects::nonNull)
                .map(HttpMethod::name)
                .collect(Collectors.joining(", "))
        ));
    }

    @Override
    public Body contentLength(long contentLength) {
        this.contentLength = contentLength;
        var value = String.valueOf(this.contentLength);
        header("content-length", value);
        return content();
    }

    @Override
    public Body content() {
        write(ByteBuffer.wrap(LN));
        return this;
    }

    @Override
    public void body(byte[] bytes) {
        checkDeclaredLength();
        if (bytes != null && bytes.length != 0) {
            int written = write(ByteBuffer.wrap(bytes));
            checkWrittenLength(written);
        }
    }

    @Override
    public void body(ReadableByteChannel channel) {
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

    private int write(ByteBuffer buffer) {
        try {
            int written = 0;
            while (buffer.hasRemaining()) {
                written += out.write(buffer);
            }
            return written;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to write " + buffer, e);
        }
    }

    private static final byte[] VERSION = "HTTP/1.1 ".getBytes();

    private static final byte[] LN = {'\r', '\n'};

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
