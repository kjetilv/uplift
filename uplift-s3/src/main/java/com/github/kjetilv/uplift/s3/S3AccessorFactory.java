package com.github.kjetilv.uplift.s3;

import com.github.kjetilv.uplift.kernel.Env;

import java.util.function.Supplier;

@FunctionalInterface
public interface S3AccessorFactory {

    S3Accessor create();

    default Supplier<S3Accessor> delayedCreate() {
        return this::create;
    }

    static S3AccessorFactory defaultFactory(Env env) {
        return new DefaultS3AccessorFactory(env);
    }
}
