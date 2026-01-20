package com.github.kjetilv.uplift.synchttp.res;

import com.github.kjetilv.uplift.synchttp.req.ResHeader;

import java.io.ByteArrayInputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public record HttpRes(
    int statusCode,
    int contentLength,
    ResHeader[] headers,
    ReadableByteChannel body
) {

    public HttpRes(int statusCode, ResHeader... headers) {
        this(statusCode, 0, headers, null);
    }

    public HttpRes(
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

    public HttpRes(
        int statusCode,
        int contentLength,
        List<ResHeader> headers,
        ReadableByteChannel body
    ) {
        this(
            statusCode,
            contentLength,
            headers.toArray(ResHeader[]::new),
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

    public ResHeader contentLengthHeader() {
        return ResHeader.contentLength(contentLength);
    }
}
