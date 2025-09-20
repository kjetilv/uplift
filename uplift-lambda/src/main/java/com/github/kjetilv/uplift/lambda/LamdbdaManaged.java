package com.github.kjetilv.uplift.lambda;

import java.io.Closeable;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
        try (LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper = looper()) {
            looper.run();
        }
    }

    LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper();
}
