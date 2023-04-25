package com.github.kjetilv.uplift.s3;

import java.util.function.Supplier;

@FunctionalInterface
public interface S3AccessorFactory {

    S3Accessor create();

    default Supplier<S3Accessor> delayedCreate() {
        return this::create;
    }
}
