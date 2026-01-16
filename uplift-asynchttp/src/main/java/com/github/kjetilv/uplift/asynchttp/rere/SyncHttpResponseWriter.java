package com.github.kjetilv.uplift.asynchttp.rere;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SyncHttpResponseWriter {

    public static final ByteBuffer LN = ByteBuffer.wrap("\n".getBytes());

    private final WritableByteChannel out;

    public SyncHttpResponseWriter(WritableByteChannel out) {
        this.out = Objects.requireNonNull(out, "out");
        write(VERSION);
    }

    public void write(HttpResponse response) {
        try (var body = response.body()) {

            var statusCode = statusCode(response);

            write(statusCode);

            for (var header : response.headers()) {
                write(header.buf());
            }

            write(LN);

            writeBody(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write to " + out, e);
        }
    }

    private void writeBody(ReadableByteChannel body) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
        while (true) {
            if (read(body, buffer)) {
                return;
            }
            write(buffer);
            buffer.flip();
            buffer.compact();
        }
    }

    private void write(ByteBuffer buffer) {
        try {
            out.write(buffer);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to write " + buffer, e);
        }
    }

    private static final Map<Integer, ByteBuffer> STATUS_CODES = new ConcurrentHashMap<>();

    private static final ByteBuffer VERSION = ByteBuffer.wrap("HTTP/1.1 ".getBytes());

    private static ByteBuffer statusCode(HttpResponse response) {
        return STATUS_CODES.computeIfAbsent(
            response.statusCode(),
            SyncHttpResponseWriter::statusBuffer
        );
    }

    private static boolean read(ReadableByteChannel body, ByteBuffer buffer) {
        try {
            return body.read(buffer) == -1 && buffer.position() <= 0;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write body " + body, e);
        }
    }

    private static ByteBuffer statusBuffer(Integer code) {
        return ByteBuffer.wrap((code + "\n").getBytes());
    }
}
