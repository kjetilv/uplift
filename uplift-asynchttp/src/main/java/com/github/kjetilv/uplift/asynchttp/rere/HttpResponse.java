package com.github.kjetilv.uplift.asynchttp.rere;

import java.nio.channels.ReadableByteChannel;
import java.util.List;

public record HttpResponse(
    int statusCode,
    List<ResponseHeader> headers,
    ReadableByteChannel body
) {
}
