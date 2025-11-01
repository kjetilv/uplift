package com.github.kjetilv.uplift.s3;

import com.github.kjetilv.uplift.kernel.Env;

@FunctionalInterface
public interface S3AccessorFactory {

    S3Accessor create();

    static S3AccessorFactory defaultFactory(Env env) {
        return new DefaultS3AccessorFactory(env);
    }
}
