package com.github.kjetilv.uplift.s3;

import module java.base;
import com.github.kjetilv.uplift.kernel.Env;

final class DefaultS3AccessorFactory implements S3AccessorFactory {

    private final Env env;

    private S3Accessor s3Accessor;

    private final Object createLock = new Object();

    DefaultS3AccessorFactory(Env env) {
        this.env = env;
    }

    /// @return The shared accessor, created on first call
    @Override
    public S3Accessor create() {
        synchronized (createLock) {
            return s3Accessor != null
                ? s3Accessor
                : (s3Accessor = S3Accessor.fromEnvironment(
                    env,
                    Executors.newVirtualThreadPerTaskExecutor()
                ));
        }
    }
}
