package com.github.kjetilv.uplift.lambda;

import module java.base;
import module java.net.http;

@SuppressWarnings("unused")
public interface LamdbdaManaged
    extends Runnable, Closeable {

    static LamdbdaManaged create(
        URI uri,
        LambdaClientSettings settings,
        LambdaHandler handler
    ) {
        return new DefaultLamdbdaManaged(uri, settings, handler);
    }

    @Override
    default void run() {
        try (var looper = looper()) {
            looper.run();
        }
    }

    LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper();
}
