package com.github.kjetilv.uplift.lambda;

import java.io.Closeable;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;

@SuppressWarnings("unused")
public interface LamdbdaManaged extends Runnable, Closeable {

    static LamdbdaManaged create(
        URI uri,
        LambdaClientSettings settings,
        LambdaHandler handler,
        ExecutorService executor
    ) {
        return new DefaultLamdbdaManaged(uri, settings, handler, executor);
    }

    @Override
    default void run() {
        try (LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper = looper()) {
            looper.run();
        }
    }

    LambdaHandler handler();

    LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper();
}
