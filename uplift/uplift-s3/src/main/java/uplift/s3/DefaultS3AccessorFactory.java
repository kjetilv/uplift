package uplift.s3;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import uplift.kernel.Env;

public final class DefaultS3AccessorFactory implements S3AccessorFactory {

    private final AtomicReference<S3Accessor> s3Accessor = new AtomicReference<>();

    private final Env env;

    private final Executor executor;

    public DefaultS3AccessorFactory(Env env, Executor executor) {
        this.env = env;
        this.executor = executor;
    }

    @Override
    public S3Accessor create() {
        return s3Accessor.updateAndGet(current ->
            current == null
                ? S3Accessor.fromEnvironment(this.env, this.executor)
                : current);
    }
}
