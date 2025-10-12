package com.github.kjetilv.uplift.asynchttp;

import module java.base;

record HttpBytes(byte[] req, byte[] headers, byte[] body) {

    static Optional<HttpBytes> read(ByteBuffer buffer) {
        var lineEnd = IntStream.range(0, buffer.position())
            .filter(oneLinebreak(buffer))
            .findFirst();

        if (lineEnd.isEmpty()) {
            return Optional.empty();
        }

        var req = extractRequestLine(buffer, lineEnd.getAsInt());

        var headerEnd = IntStream.range(
                lineEnd.getAsInt() + 2,
                buffer.position() - 1
            )
            .filter(twoLinebreaks(buffer)
            )
            .findFirst();

        if (headerEnd.isEmpty()) {
            var headers = extractHeaders(
                buffer,
                lineEnd.getAsInt(),
                buffer.position()
            );
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
        return index -> crlf(buffer, index);
    }

    private static IntPredicate twoLinebreaks(ByteBuffer buf) {
        return idx -> crlf(buf, idx) && crlf(buf, idx + 2);
    }

    private static byte[] extractRequestLine(ByteBuffer buffer, int lineEnd) {
        var req = new byte[lineEnd];
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
        var body = new byte[length];
        buffer.get(start, body, 0, length);
        return body;
    }

    private static boolean crlf(ByteBuffer buf, int idx) {
        return cr(buf, idx) && lf(buf, idx + 1);
    }

    private static boolean cr(ByteBuffer buffer, int index) {
        return buffer.get(index) == '\r';
    }

    private static boolean lf(ByteBuffer buffer, int index) {
        return buffer.get(index) == '\n';
    }
}
