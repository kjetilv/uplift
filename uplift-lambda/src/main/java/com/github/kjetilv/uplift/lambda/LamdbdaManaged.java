package com.github.kjetilv.uplift.lambda;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SuppressWarnings("unused")
public interface LamdbdaManaged extends Runnable {

    @Override
    default void run() {
        try (LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper = looper()) {
            looper.run();
        }
    }

    LambdaHandler handler();

    LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper();
}
