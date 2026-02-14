package com.github.kjetilv.uplift.synchttp;

import java.util.Map;

import static com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback.Headers;

public abstract class AbstractHttpHandler implements HttpHandler {

    private final Map<String, Object> fixedHeaders;

    protected AbstractHttpHandler(Map<String, Object> fixedHeaders) {
        this.fixedHeaders = fixedHeaders == null || fixedHeaders.isEmpty()
            ? Map.of()
            : Map.copyOf(fixedHeaders);
    }

    protected Headers withFixed(Headers headers) {
        return headers.headers().headers(fixedHeaders);
    }
}
