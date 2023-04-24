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
    Executor lambdaExecutor,
    Executor serverExecutor,
    Supplier<Instant> time
) {

    public LambdaClientSettings(
        Env env,
        Duration connectTimeout,
        Executor lambdaExecutor,
        Executor serverExecutor,
        Supplier<Instant> time
    ) {
        this.env = requireNonNull(env, "env");
        this.connectTimeout = connectTimeout == null ? Duration.ofMinutes(1) : connectTimeout;
        this.lambdaExecutor = requireNonNull(lambdaExecutor, "lambdaExecutor");
        this.serverExecutor = requireNonNull(serverExecutor, "serverExecutor");
        this.time = requireNonNull(time, "time");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + env + ", connect timeout: " + connectTimeout + "]";
    }
}
