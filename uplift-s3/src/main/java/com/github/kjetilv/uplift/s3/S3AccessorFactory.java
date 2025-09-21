package com.github.kjetilv.uplift.s3;

import module java.base;
import module uplift.kernel;

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
