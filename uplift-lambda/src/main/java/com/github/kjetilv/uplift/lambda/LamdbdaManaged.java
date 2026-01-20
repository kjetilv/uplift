package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.util.RuntimeCloseable;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SuppressWarnings("unused")
public interface LamdbdaManaged
    extends Runnable, RuntimeCloseable {

    @Override
    default void run() {
        try (var looper = looper()) {
            looper.run();
        }
    }

    URI lambdaUri();

    LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper();
}
