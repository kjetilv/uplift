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

import com.github.kjetilv.uplift.asynchttp.HttpChannelHandler;
import com.github.kjetilv.uplift.lambda.DefaultLamdbdaManaged;
import com.github.kjetilv.uplift.lambda.LambdaClientSettings;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import com.github.kjetilv.uplift.lambda.LambdaLooper;
import com.github.kjetilv.uplift.lambda.LamdbdaManaged;

import static com.github.kjetilv.uplift.kernel.ManagedExecutors.executor;

@SuppressWarnings("unused")
public class LambdaTestHarness implements Closeable {

    private final String name;

    private final ExecutorService testExec;

    private final LocalLambda localLambda;

    private final ExecutorService lambdaExec;

    private final ExecutorService serverExec;

    private final LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper;

    private final HttpChannelHandler.R r;

    public LambdaTestHarness(String name, LambdaHandler lambdaHandler) {
        this(name, lambdaHandler, null, null);
    }

    public LambdaTestHarness(String name, LambdaHandler lambdaHandler,  Supplier<Instant> time) {
        this(name, lambdaHandler, null, time);
    }

    public LambdaTestHarness(String name, LambdaHandler lambdaHandler, CorsSettings cors, Supplier<Instant> time) {
        Objects.requireNonNull(lambdaHandler, "lambdaHandler");
        this.name = name == null ? lambdaHandler.getClass().getSimpleName() : name;

        serverExec = executor(this.name + "-S", 5);
        lambdaExec = executor(this.name + "-L", 5);
        testExec = executor(this.name, 4);

        Supplier<Instant> timeRetriever = time == null ? Instant::now : time;
        localLambda = new LocalLambda(new LocalLambdaSettings(
            0,
            0,
            8 * 8192,
            10,
            lambdaExec,
            serverExec,
            cors == null ? CORS_DEFAULTS : cors,
            timeRetriever
        ));

        testExec.submit(localLambda);

        LambdaClientSettings lambdaClientSettings = new LambdaClientSettings(
            new EmptyEnv(),
            lambdaExec,
            serverExec,
            timeRetriever
        );
        LamdbdaManaged lamdbdaManaged = new DefaultLamdbdaManaged(
            localLambda.getLambdaUri(),
            lambdaClientSettings,
            lambdaHandler
        );
        looper = lamdbdaManaged.looper();

        testExec.submit(looper);
        r = localLambda.r();
    }

    @Override
    public void close() {
        localLambda.close();
        Stream.of(serverExec, lambdaExec, testExec)
            .forEach(ExecutorService::shutdown);

        looper.close();
        localLambda.join();
    }

    public HttpChannelHandler.R r() {
        return r;
    }

    public static final CorsSettings CORS_DEFAULTS = new CorsSettings(
        List.of("*"),
        List.of("GET", "POST", "PUT", "DELETE", "HEAD"),
        List.of("content-type")
    );

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + " @ " + localLambda.getLambdaUri() + "]";
    }
}
