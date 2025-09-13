package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.util.Time;

import java.time.Duration;

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
        try (
            LamdbdaManaged managed = managed(
                lambdaHandler,
                connectTimeout,
                responseTimeout
            )
        ) {
            managed.run();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to run lambda: " + lambdaHandler, e);
        }
    }

    public static LamdbdaManaged managed(
        LambdaHandler lambdaHandler,
        Duration connectTimeout,
        Duration responseTimeout
    ) {
        return LamdbdaManaged.create(
            Env.actual().awsLambdaUri(),
            settings(Env.actual(), connectTimeout, responseTimeout),
            lambdaHandler
        );
    }

    private Lambda() {
    }

    private static LambdaClientSettings settings(
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
