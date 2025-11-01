package com.github.kjetilv.uplift.s3;

import module java.base;
import com.github.kjetilv.uplift.kernel.Env;

final class DefaultS3AccessorFactory implements S3AccessorFactory {

    private final AtomicReference<S3Accessor> s3Accessor = new AtomicReference<>();

    private final Env env;

    DefaultS3AccessorFactory(Env env) {
        this.env = env;
    }

    @Override
    public S3Accessor create() {
        return s3Accessor.updateAndGet(current ->
            current == null
                ? S3Accessor.fromEnvironment(this.env, VIRTUAL_THREADS)
                : current);
    }

    private static final ExecutorService VIRTUAL_THREADS = Executors.newSingleThreadExecutor();
}
