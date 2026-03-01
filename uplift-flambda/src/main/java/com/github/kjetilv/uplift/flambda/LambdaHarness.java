package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.lambda.Lambda;
import com.github.kjetilv.uplift.lambda.LambdaClientSettings;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import com.github.kjetilv.uplift.lambda.LambdaLooper;
import com.github.kjetilv.uplift.synchttp.CorsSettings;
import com.github.kjetilv.uplift.util.RuntimeCloseable;

@SuppressWarnings("unused")
public class LambdaHarness implements RuntimeCloseable {

    public static final CorsSettings CORS_DEFAULTS = new CorsSettings(
        List.of("*"),
        List.of("GET"),
        Collections.emptyList()
    );

    private final String name;

    private final Flambda flambda;

    private final LambdaLooper looper;

    private final Reqs reqs;

    private LambdaHandler handler;

    public LambdaHarness(
        String name,
        LambdaHandler lambdaHandler
    ) {
        this(
            name,
            lambdaHandler,
            null,
            null,
            null,
            null
        );
    }

    public LambdaHarness(
        String name,
        LambdaHandler lambdaHandler,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        this(
            name,
            lambdaHandler,
            null,
            null,
            cors,
            time
        );
    }

    @SuppressWarnings("resource")
    private LambdaHarness(
        String name,
        LambdaHandler lambdaHandler,
        FlambdaSettings flambdaSettings,
        LambdaClientSettings lambdaClientSettings,
        CorsSettings corsSettings,
        Supplier<Instant> time
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.handler = Objects.requireNonNull(lambdaHandler, "lambdaHandler");

        FlambdaSettings settings = flambdaSettings != null ? flambdaSettings
            : new FlambdaSettings(
                name,
                _64K,
                SHORT_Q,
                corsSettings != null ? corsSettings
                    : CORS_DEFAULTS,
                resolve(time)
            );

        var clientSettings = lambdaClientSettings != null
            ? lambdaClientSettings.time(time)
            : new LambdaClientSettings(new EmptyEnv(), time);

        this.flambda = new Flambda(settings);
        var managed = Lambda.managed(
            this.flambda.lambdaUri(),
            clientSettings,
            handler
        );

        this.looper = managed.looper(name);
        Executors.newVirtualThreadPerTaskExecutor().submit(this.looper);
        this.reqs = flambda.reqs();
    }

    @Override
    public void close() {
        this.flambda.close();
        this.looper.close();
    }

    public Reqs reqs() {
        return this.reqs;
    }

    private static final int _64K = 65536;

    private static final int SHORT_Q = 10;

    private static final Supplier<Instant> SYSTEM_TIME = Instant::now;

    private static Supplier<Instant> resolve(Supplier<Instant> time) {
        return time == null ? SYSTEM_TIME : time;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               name + "/" + handler + " @ " +
               flambda.apiUri() + " -> λ" + flambda.lambdaUri().getPort() + "]";
    }
}
