package com.github.kjetilv.uplift.lambda;

import java.io.Closeable;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SuppressWarnings("unused")
public interface LamdbdaManaged
    extends Runnable, Closeable {

    @Override
    default void run() {
        try (var looper = looper()) {
            looper.run();
        }
    }

    URI lambdaUri();

    LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper();
}
