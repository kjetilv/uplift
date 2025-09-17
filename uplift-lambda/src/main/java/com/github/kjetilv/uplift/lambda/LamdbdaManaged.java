package com.github.kjetilv.uplift.lambda;

import java.io.Closeable;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface LamdbdaManaged
    extends Runnable, Closeable, Supplier<LambdaLooper<HttpRequest, HttpResponse<InputStream>>> {

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

    @Override
    default LambdaLooper<HttpRequest, HttpResponse<InputStream>> get() {
        return looper();
    }

    LambdaHandler handler();

    LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper();
}
