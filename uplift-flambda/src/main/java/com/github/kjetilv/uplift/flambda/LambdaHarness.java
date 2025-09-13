package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.lambda.LambdaClientSettings;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import com.github.kjetilv.uplift.lambda.LambdaLooper;
import com.github.kjetilv.uplift.lambda.LamdbdaManaged;

import java.io.Closeable;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.kernel.ManagedExecutors.executor;

@SuppressWarnings("unused")
public class LambdaHarness implements Closeable {

    public static final CorsSettings CORS_DEFAULTS = new CorsSettings(
        List.of("*"),
        List.of("GET"),
        Collections.emptyList()
    );

    private final String name;

    private final ExecutorService testExec;

    private final LocalLambda localLambda;

    private final ExecutorService lambdaExec;

    private final ExecutorService serverExec;

    private final LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper;

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

    public LambdaHarness(String name, LambdaHandler lambdaHandler, Supplier<Instant> time) {
        this(
            name,
            lambdaHandler,
            null,
            null,
            null,
            time
        );
    }

    public LambdaHarness(String name, LambdaHandler lambdaHandler, CorsSettings cors, Supplier<Instant> time) {
        this(
            name,
            lambdaHandler,
            null,
            null,
            cors,
            time
        );
    }

    public LambdaHarness(String name, LambdaHandler lambdaHandler, LocalLambdaSettings localLambdaSettings) {
        this(
            name,
            lambdaHandler,
            localLambdaSettings,
            null
        );
    }

    public LambdaHarness(
        String name,
        LambdaHandler lambdaHandler,
        LocalLambdaSettings localLambdaSettings,
        LambdaClientSettings lambdaClientSettings
    ) {
        this(
            name,
            lambdaHandler,
            localLambdaSettings,
            lambdaClientSettings,
            null,
            null
        );
    }

    private LambdaHarness(
        String name,
        LambdaHandler lambdaHandler,
        LocalLambdaSettings localLambdaSettings,
        LambdaClientSettings lambdaClientSettings,
        CorsSettings corsSettings,
        Supplier<Instant> time
    ) {
        this.name = Objects.requireNonNull(name, "name");
        Objects.requireNonNull(lambdaHandler, "lambdaHandler");

        this.serverExec = executor();
        this.lambdaExec = executor();
        this.testExec = executor();

        LocalLambdaSettings settings = localLambdaSettings == null
            ? settings(localLambdaSettings, corsSettings, time)
            : localLambdaSettings;

        this.localLambda = new LocalLambda(settings);
        this.testExec.submit(localLambda);
        this.localLambda.awaitStarted(Duration.ofMinutes(1));

        LamdbdaManaged lamdbdaManaged = LamdbdaManaged.create(
            localLambda.getLambdaUri(),
            adjustedSettings(lambdaClientSettings, settings.time()),
            lambdaHandler
        );

        this.looper = lamdbdaManaged.looper();
        this.testExec.submit(this.looper);
        this.reqs = this.localLambda.reqs();
    }

    @Override
    public void close() {
        this.localLambda.close();
        Stream.of(this.serverExec, this.lambdaExec, this.testExec)
            .forEach(ExecutorService::shutdown);

        this.looper.close();
        this.localLambda.join();
    }

    public Reqs reqs() {
        return this.reqs;
    }

    private static final int K_64 = 8 * 8192;

    private static final int SHORT_Q = 10;

    private static final Supplier<Instant> SYSTEM_TIME = Instant::now;

    private static LambdaClientSettings adjustedSettings(
        LambdaClientSettings settings,
        Supplier<Instant> time
    ) {
        return settings == null
            ? new LambdaClientSettings(new EmptyEnv(), time)
            : new LambdaClientSettings(settings.env(), settings.connectTimeout(), settings.responseTimeout(), time);
    }

    private static LocalLambdaSettings settings(
        LocalLambdaSettings localLambdaSettings,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        return new LocalLambdaSettings(
            localLambdaSettings == null ? null : localLambdaSettings.lambdaPort(),
            localLambdaSettings == null ? null : localLambdaSettings.apiPort(),
            localLambdaSettings == null
                ? K_64
                : localLambdaSettings.requestBufferSize(),
            localLambdaSettings == null
                ? SHORT_Q
                : localLambdaSettings.queueLength(),
            cors != null ? cors
                : localLambdaSettings != null ? localLambdaSettings.cors()
                    : CORS_DEFAULTS,
            resolve(time != null ? time
                : localLambdaSettings != null ? localLambdaSettings.time()
                    : null)
        );
    }

    private static Supplier<Instant> resolve(Supplier<Instant> time) {
        return time == null ? SYSTEM_TIME : time;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + " @ " + localLambda.getLambdaUri() + "]";
    }
}
