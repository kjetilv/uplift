package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.kernel.Env;

import java.time.Duration;
import java.time.InstantSource;

import static java.util.Objects.requireNonNull;

public record LambdaClientSettings(
    Env env,
    Duration connectTimeout,
    Duration responseTimeout,
    InstantSource time
) {

    public LambdaClientSettings(Env env, InstantSource time) {
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
        InstantSource time
    ) {
        this.env = requireNonNull(env, "env");
        this.connectTimeout = sane(connectTimeout);
        this.responseTimeout = sane(responseTimeout);
        this.time = requireNonNull(time, "time");
    }

    public boolean hasConnectTimeout() {
        return connectTimeout.compareTo(Duration.ZERO) > 0;
    }

    public LambdaClientSettings time(InstantSource time) {
        return new LambdaClientSettings(env(), connectTimeout(), responseTimeout(), time);
    }

    private static Duration sane(Duration timeout) {
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ZERO : timeout;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + env + " timeout: " + connectTimeout + "/" + responseTimeout + "]";
    }
}
