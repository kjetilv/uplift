package com.github.kjetilv.uplift.lambda;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import com.github.kjetilv.uplift.kernel.Env;

import static java.util.Objects.requireNonNull;

public record LambdaClientSettings(
    Env env,
    Duration connectTimeout,
    Duration responseTimeout,
    Supplier<Instant> time
) {

    public LambdaClientSettings(Env env, Supplier<Instant> time) {
        this(
            env,
            null,
            null,
            time
        );
    }

    public LambdaClientSettings(
        Env env,
        Duration connectTimeout,
        Duration responseTimeout,
        Supplier<Instant> time
    ) {
        this.env = requireNonNull(env, "env");
        this.connectTimeout = sane(connectTimeout);
        this.responseTimeout = sane(responseTimeout);
        this.time = requireNonNull(time, "time");
    }

    public boolean hasConnectTimeout() {
        return connectTimeout.compareTo(Duration.ZERO) > 0;
    }

    private static Duration sane(Duration timeout) {
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ZERO : timeout;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + env + " timeout: " + connectTimeout + "/" + responseTimeout + "]";
    }
}
