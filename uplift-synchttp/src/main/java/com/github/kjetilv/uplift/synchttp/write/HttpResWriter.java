package com.github.kjetilv.uplift.synchttp.write;

import com.github.kjetilv.uplift.synchttp.res.HttpRes;
import com.github.kjetilv.uplift.synchttp.util.Utils;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

public class HttpResWriter {

    private final WritableByteChannel out;

    public HttpResWriter(WritableByteChannel out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    public void write(HttpRes response) {
        try (this.out; var body = response.body()) {
            write(ByteBuffer.wrap(VERSION));
            write(statusCode(response));

            var wroteContentLength = false;
            for (var header : response.headers()) {
                if (header.isContentLength()) {
                    wroteContentLength = true;
                }
                write(header.buffer());
            }

            if (body != null) {
                if (!wroteContentLength) {
                    write(response.contentLengthHeader().buffer());
                }
                write(ByteBuffer.wrap(LN));
                var written = writeBody(body);
                if (written != response.contentLength()) {
                    throw new IllegalStateException(
                        "Expected " + response.contentLength() + " bytes, but wrote " + written + ": " + response);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write " + response, e);
        }
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
        int written = 0;
        try {
            while (buffer.hasRemaining()) {
                written += out.write(buffer);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to write " + buffer, e);
        }
        return written;
    }

    private static final byte[] VERSION = "HTTP/1.1 ".getBytes();

    private static final byte[] LN = {'\r', '\n'};

    private static ByteBuffer statusCode(HttpRes response) {
        var statusCode = response.statusCode() + "\r\n";
        return ByteBuffer.wrap(statusCode.getBytes());
    }
}
