package com.github.kjetilv.uplift.lambda;

import java.time.Duration;

import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.Time;

import static com.github.kjetilv.uplift.kernel.ManagedExecutors.executor;

@SuppressWarnings("unused")
public final class Lambda {

    public static final Duration CONNECT_TIMEOUT = Duration.ofMinutes(1);

    public static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(1);

    public static final int PARALLELLISM = 4;

    public static void simply(LambdaHandler lambdaHandler) {
        simply(lambdaHandler, null, null, 0);
    }

    public static void simply(
        LambdaHandler lambdaHandler,
        Duration connectTimeout,
        Duration responseTimeout,
        int parallellism
    ) {
        managed(lambdaHandler, connectTimeout, responseTimeout, parallellism).run();
    }

    public static LamdbdaManaged managed(
        LambdaHandler lambdaHandler,
        Duration connectTimeout,
        Duration responseTimeout,
        int parallellism
    ) {
        Env env = Env.actual();
        LamdbdaManaged lamdbdaManaged = new DefaultLamdbdaManaged(
            env.awsLambdaUri(),
            getSettings(env, connectTimeout, responseTimeout, parallellism),
            lambdaHandler);
        return lamdbdaManaged;
    }

    private Lambda() {
    }

    private static LambdaClientSettings getSettings(
        Env env,
        Duration connectTimeout,
        Duration responseTimeout,
        int parallellism
    ) {
        return new LambdaClientSettings(
            env,
            connectTimeout,
            responseTimeout,
            executor("L", parallellism > 0 ? parallellism : PARALLELLISM),
            executor("S", parallellism > 0 ? parallellism : PARALLELLISM),
            Time.utcSupplier());
    }
}
