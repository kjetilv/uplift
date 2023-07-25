package com.github.kjetilv.uplift.lambda;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.github.kjetilv.uplift.kernel.Env;

import static java.util.Objects.requireNonNull;

public record LambdaClientSettings(
    Env env,
    Duration connectTimeout,
    Duration responseTimeout,
    Executor lambdaExecutor,
    Executor serverExecutor,
    Supplier<Instant> time
) {

    public LambdaClientSettings(
        Env env,
        Executor lambdaExecutor,
        Executor serverExecutor,
        Supplier<Instant> time
    ) {
        this(
            env,
            null,
            null,
            lambdaExecutor,
            serverExecutor,
            time
        );
    }

    public LambdaClientSettings(
        Env env,
        Duration connectTimeout,
        Duration responseTimeout,
        Executor lambdaExecutor,
        Executor serverExecutor,
        Supplier<Instant> time
    ) {
        this.env = requireNonNull(env, "env");
        this.connectTimeout = sane(connectTimeout);
        this.responseTimeout = sane(responseTimeout);
        this.lambdaExecutor = requireNonNull(lambdaExecutor, "lambdaExecutor");
        this.serverExecutor = requireNonNull(serverExecutor, "serverExecutor");
        this.time = requireNonNull(time, "time");
    }

    private static Duration sane(Duration timeout) {
        return timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ZERO : timeout;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + env + " timeout: " + connectTimeout + "/" + responseTimeout + "]";
    }
}
