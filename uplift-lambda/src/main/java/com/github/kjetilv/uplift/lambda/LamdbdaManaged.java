package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.util.RuntimeCloseable;

import java.net.URI;
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

    LambdaLooper looper(String name);
}
