package com.github.kjetilv.uplift.asynchttp.rere;

import java.nio.channels.ReadableByteChannel;
import java.util.List;

public record HttpResponse(
    int statusCode,
    ResponseHeader[] headers,
    ReadableByteChannel body
) {

    public HttpResponse(
        int statusCode,
        List<ResponseHeader> headers,
        ReadableByteChannel body
    ) {
        this(
            statusCode,
            headers.toArray(ResponseHeader[]::new),
            body
        );
    }
}
