package com.github.kjetilv.uplift.lambda;

import java.time.Duration;

import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.Time;

import static com.github.kjetilv.uplift.kernel.ManagedExecutors.executor;

@SuppressWarnings("unused")
public final class Lambda {

    public static void simply(LambdaHandler lambdaHandler) {
        LambdaClientSettings clientSettings = new LambdaClientSettings(
            Env.actual(),
            Duration.ofMinutes(1),
            executor("L", 10),
            executor("S", 10),
            Time.utcSupplier()
        );
        new LamdbdaManaged(
            Env.actual().awsLambdaUri(),
            clientSettings,
            lambdaHandler
        ).run();
    }

    private Lambda() {

    }
}
