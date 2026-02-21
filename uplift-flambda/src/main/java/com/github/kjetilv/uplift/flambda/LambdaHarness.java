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

    private final ExecutorService testExec;

    private final ExecutorService lambdaExec;

    private final ExecutorService serverExec;

    private final LambdaLooper looper;

    private final Reqs reqs;

    public LambdaHarness(String name, LambdaHandler lambdaHandler) {
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
        Objects.requireNonNull(lambdaHandler, "lambdaHandler");

        var executor = Executors.newVirtualThreadPerTaskExecutor();
        this.serverExec = executor;
        this.lambdaExec = executor;
        this.testExec = executor;

        var settings = flambdaSettings == null
            ? settings(name, corsSettings, time)
            : flambdaSettings;

        var clientSettings = lambdaClientSettings == null
            ? new LambdaClientSettings(new EmptyEnv(), time)
            : lambdaClientSettings.time(time);

        this.flambda = new Flambda(settings);
        Supplier<Instant> time1 = settings.time();
        this.looper = Lambda.managed(
            this.flambda.lambdaUri(),
            clientSettings,
            lambdaHandler
        ).looper(name);
        this.testExec.submit(this.looper);
        this.reqs = flambda.reqs();
    }

    @Override
    public void close() {
        this.flambda.close();
        Stream.of(this.serverExec, this.lambdaExec, this.testExec)
            .forEach(ExecutorService::shutdown);
        this.looper.close();
    }

    public Reqs reqs() {
        return this.reqs;
    }

    private static final int _64K = 65536;

    private static final int SHORT_Q = 10;

    private static final Supplier<Instant> SYSTEM_TIME = Instant::now;

    private static FlambdaSettings settings(String name, CorsSettings cors, Supplier<Instant> time) {
        return new FlambdaSettings(
            name,
            65536,
            10,
            cors != null ? cors
                : CORS_DEFAULTS,
            resolve(time)
        );
    }

    private static Supplier<Instant> resolve(Supplier<Instant> time) {
        return time == null ? SYSTEM_TIME : time;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + " @ " + flambda.apiUri() + " -> " + flambda.lambdaUri() + "]";
    }
}
