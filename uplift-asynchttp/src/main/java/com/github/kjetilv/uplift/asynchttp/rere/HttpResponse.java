package com.github.kjetilv.uplift.asynchttp.rere;

import java.io.ByteArrayInputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public record HttpResponse(
    int statusCode,
    int contentLength,
    ResponseHeader[] headers,
    ReadableByteChannel body
) {

    public HttpResponse(int statusCode, ResponseHeader... headers) {
        this(statusCode, 0, headers, null);
    }

    public HttpResponse(
        int statusCode,
        String body
    ) {
        this(
            statusCode,
            body.length(),
            List.of(),
            Channels.newChannel(new ByteArrayInputStream(body.getBytes(UTF_8)))
        );
    }

    public HttpResponse(
        int statusCode,
        int contentLength,
        List<ResponseHeader> headers,
        ReadableByteChannel body
    ) {
        this(
            statusCode,
            contentLength,
            headers.toArray(ResponseHeader[]::new),
            body
        );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + statusCode +
               (body == null ? "": " length=" + contentLength) +
               " headers=" + Arrays.toString(headers) +
               "]";
    }

    public ResponseHeader contentLengthHeader() {
        return ResponseHeader.contentLength(contentLength);
    }
}
