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
        return new DefaultLamdbdaManaged(
            env.awsLambdaUri(),
            setttings(env, connectTimeout, responseTimeout),
            lambdaHandler,
            executor("L", parallellism > 0 ? parallellism : PARALLELLISM)
        );
    }

    private Lambda() {
    }

    private static LambdaClientSettings setttings(
        Env env,
        Duration connectTimeout,
        Duration responseTimeout
    ) {
        return new LambdaClientSettings(
            env,
            connectTimeout,
            responseTimeout,
            Time.utcSupplier()
        );
    }
}
