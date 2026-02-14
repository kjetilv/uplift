package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.util.RuntimeCloseable;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface LamdbdaManaged
    extends Consumer<String>, RuntimeCloseable {

    @Override
    default void accept(String name) {
        try (var looper = looper(name)) {
            looper.run();
        }
    }

    URI lambdaUri();

    LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper(String name);
}
