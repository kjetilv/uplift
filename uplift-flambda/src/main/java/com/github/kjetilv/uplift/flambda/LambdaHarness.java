package com.github.kjetilv.uplift.flambda;

import java.io.Closeable;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.kjetilv.uplift.lambda.DefaultLamdbdaManaged;
import com.github.kjetilv.uplift.lambda.LambdaClientSettings;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import com.github.kjetilv.uplift.lambda.LambdaLooper;
import com.github.kjetilv.uplift.lambda.LamdbdaManaged;

import static com.github.kjetilv.uplift.kernel.ManagedExecutors.executor;

@SuppressWarnings("unused")
public class LambdaHarness implements Closeable {

    public static final CorsSettings CORS_DEFAULTS = new CorsSettings(
        List.of("*"),
        List.of("GET", "POST", "PUT", "DELETE", "HEAD"),
        List.of("content-type")
    );

    private final String testName;

    private final ExecutorService testExec;

    private final LocalLambda localLambda;

    private final ExecutorService lambdaExec;

    private final ExecutorService serverExec;

    private final LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper;

    private final Reqs reqs;

    public LambdaHarness(LambdaHandler lambdaHandler) {
        this(null, lambdaHandler);
    }

    public LambdaHarness(
        String testName,
        LambdaHandler lambdaHandler
    ) {
        this(
            testName,
            lambdaHandler,
            null,
            null,
            null,
            null
        );
    }

    public LambdaHarness(
        String testName,
        LambdaHandler lambdaHandler,
        Supplier<Instant> time
    ) {
        this(
            testName,
            lambdaHandler,
            null,
            null,
            null,
            time
        );
    }

    public LambdaHarness(
        LambdaHandler lambdaHandler,
        CorsSettings cors
    ) {
        this(
            null,
            lambdaHandler,
            null,
            null,
            cors,
            null
        );
    }

    public LambdaHarness(
        String testName,
        LambdaHandler lambdaHandler,
        CorsSettings cors,
        Supplier<Instant> time
    ) {
        this(
            testName,
            lambdaHandler,
            null,
            null,
            cors,
            time
        );
    }

    public LambdaHarness(
        String testName,
        LambdaHandler lambdaHandler,
        LocalLambdaSettings localLambdaSettings
    ) {
        this(
            testName,
            lambdaHandler,
            localLambdaSettings,
            null
        );
    }

    public LambdaHarness(
        String testName,
        LambdaHandler lambdaHandler,
        LocalLambdaSettings localLambdaSettings,
        LambdaClientSettings lambdaClientSettings
    ) {
        this(
            testName,
            lambdaHandler,
            localLambdaSettings,
            lambdaClientSettings,
            null,
            null
        );
    }

    private LambdaHarness(
        String testName,
        LambdaHandler lambdaHandler,
        LocalLambdaSettings localLambdaSettings,
        LambdaClientSettings lambdaClientSettings,
        CorsSettings corsSettings,
        Supplier<Instant> time
    ) {
        Objects.requireNonNull(lambdaHandler, "lambdaHandler");
        this.testName = testName == null ? lambdaHandler.getClass().getSimpleName() : testName;

        this.serverExec = executor(this.testName + "-S", 5);
        this.lambdaExec = executor(this.testName + "-L", 5);
        this.testExec = executor(this.testName, 4);

        LocalLambdaSettings settings = localLambdaSettings == null
            ? settings(corsSettings, time)
            : localLambdaSettings;

        this.localLambda = new LocalLambda(settings);
        this.testExec.submit(localLambda);
        this.localLambda.awaitStarted(Duration.ofMinutes(1));

        LamdbdaManaged lamdbdaManaged = new DefaultLamdbdaManaged(
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

    private LambdaClientSettings adjustedSettings(
        LambdaClientSettings base, Supplier<Instant> time
    ) {
        return new LambdaClientSettings(
            base == null ? new EmptyEnv() : base.env(),
            base == null ? null : base.connectTimeout(),
            base == null ? null : base.responseTimeout(),
            lambdaExec,
            serverExec,
            time
        );
    }

    private LocalLambdaSettings settings(CorsSettings cors, Supplier<Instant> time) {
        return new LocalLambdaSettings(
            8 * 8192,
            10,
            lambdaExec,
            serverExec,
            cors == null ? CORS_DEFAULTS : cors,
            resolve(time)
        );
    }

    private static final Supplier<Instant> SYSTEM_TIME = Instant::now;

    private static Supplier<Instant> resolve(Supplier<Instant> time) {
        return time == null ? SYSTEM_TIME : time;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + testName + " @ " + localLambda.getLambdaUri() + "]";
    }
}
