package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.util.Time;

import java.net.URI;
import java.time.Duration;

@SuppressWarnings("unused")
public final class Lambda {

    public static final Duration CONNECT_TIMEOUT = Duration.ofMinutes(1);

    public static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(1);

    public static final int PARALLELLISM = 4;

    public static void simply(String name, LambdaHandler lambdaHandler) {
        simply(name, lambdaHandler, null, null, 0);
    }

    public static void simply(
        String name,
        LambdaHandler handler,
        Duration connectTimeout,
        Duration responseTimeout,
        int parallellism
    ) {
        try (
            var managed = managed(handler, connectTimeout, responseTimeout)
        ) {
            managed.accept(name);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to run lambda: " + handler, e);
        }
    }

    public static LamdbdaManaged managed(
        LambdaHandler handler,
        Duration connectTimeout,
        Duration responseTimeout
    ) {
        var env = Env.actual();
        return managed(
            env.awsLambdaUri(),
            new LambdaClientSettings(
                env,
                connectTimeout,
                responseTimeout,
                Time.utcSupplier()
            ),
            handler
        );
    }

    public static LamdbdaManaged managed(
        URI uri,
        LambdaClientSettings settings,
        LambdaHandler handler
    ) {
        return new DefaultLamdbdaManaged(uri, settings, handler);
    }

    private Lambda() {
    }

}
