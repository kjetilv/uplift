package com.github.kjetilv.uplift.asynchttp;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

record HttpBytes(byte[] req, byte[] headers, byte[] body) {

    static Optional<HttpBytes> read(ByteBuffer buffer) {
        OptionalInt lineEnd = IntStream.range(0, buffer.position())
            .filter(oneLinebreak(buffer))
            .findFirst();

        if (lineEnd.isEmpty()) {
            return Optional.empty();
        }

        byte[] req = extractRequestLine(buffer, lineEnd.getAsInt());

        OptionalInt headerEnd =
            IntStream.range(lineEnd.getAsInt() + 2, buffer.position() - 1).filter(twoLinebreaks(
                buffer)).findFirst();

        if (headerEnd.isEmpty()) {
            byte[] headers = extractHeaders(buffer, lineEnd.getAsInt(), buffer.position());
            return Optional.of(new HttpBytes(req, headers));
        }

        return Optional.of(new HttpBytes(
            req,
            extractHeaders(buffer, lineEnd.getAsInt(), headerEnd.getAsInt()),
            extractBody(buffer, headerEnd.getAsInt())
        ));
    }

    private HttpBytes(byte[] req, byte[] headers) {
        this(req, headers, null);
    }

    private static IntPredicate oneLinebreak(ByteBuffer buffer) {
        return index -> cr(buffer, index) && lf(buffer, index + 1);
    }

    private static IntPredicate twoLinebreaks(ByteBuffer buf) {
        return idx ->
            cr(buf, idx) && lf(buf, idx + 1) && cr(buf, idx + 2) && lf(buf, idx + 3);
    }

    private static byte[] extractRequestLine(ByteBuffer buffer, int lineEnd) {
        byte[] req = new byte[lineEnd];
        buffer.get(0, req, 0, lineEnd);
        return req;
    }

    private static byte[] extractHeaders(ByteBuffer buffer, int lineEnd, int headerEnd) {
        return fill(buffer, lineEnd + 2, headerEnd - lineEnd - 2);
    }

    private static byte[] extractBody(ByteBuffer buffer, int headerEnd) {
        return fill(buffer, headerEnd + 4, buffer.position() - headerEnd - 4);
    }

    private static byte[] fill(ByteBuffer buffer, int start, int length) {
        byte[] body = new byte[length];
        buffer.get(start, body, 0, length);
        return body;
    }

    private static boolean cr(ByteBuffer buffer, int index) {
        return buffer.get(index) == '\r';
    }

    private static boolean lf(ByteBuffer buffer, int index) {
        return buffer.get(index) == '\n';
    }
}
