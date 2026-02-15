package com.github.kjetilv.uplift.synchttp.rere;

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

    public static final ResHeader[] NO_HEADERS = new ResHeader[0];

    public HttpRes(int statusCode, ResHeader... headers) {
        this(
            statusCode,
            0,
            headers,
            null
        );
    }

    public HttpRes(int statusCode, String body) {
        this(
            statusCode,
            body.length(),
            (ResHeader[]) null,
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
            headers == null ? null : headers.toArray(ResHeader[]::new),
            body
        );
    }

    public HttpRes(
        int statusCode,
        int contentLength,
        ResHeader[] headers,
        ReadableByteChannel body
    ) {
        this.statusCode = statusCode;
        this.contentLength = contentLength;
        this.headers = headers == null ? NO_HEADERS : headers;
        this.body = body;
    }

    public ResHeader contentLengthHeader() {
        return ResHeader.contentLength(contentLength);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + statusCode +
               (body == null ? "" : " length=" + contentLength) +
               " headers=" + Arrays.toString(headers) +
               "]";
    }
}
