package com.github.kjetilv.uplift.flambda;

import java.io.Closeable;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
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

    private ExecutorService testExecutor;

    private LocalLambda localLambda;

    private ExecutorService lambdaExec;

    private ExecutorService serverExec;

    private LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper;

    private HttpChannelHandler.R r;

    public LambdaTestHarness(String name, LambdaHandler lambdaHandler) {
        this(name, lambdaHandler, null);
    }

    public LambdaTestHarness(String name, LambdaHandler lambdaHandler, CorsSettings cors) {
        testExecutor = executor(name, 4);
        serverExec = executor(name + "-S", 5);
        lambdaExec = executor(name + "-L", 5);
        this.name = name;

        localLambda = new LocalLambda(new LocalLambdaSettings(
            0,
            0,
            8 * 8192,
            10,
            lambdaExec,
            serverExec,
            cors == null ? CORS_DEFAULTS : cors,
            Instant::now
        ));

        LambdaClientSettings lambdaClientSettings = new LambdaClientSettings(
            new EmptyEnv(),
            Duration.ofSeconds(10),
            lambdaExec,
            serverExec,
            Instant::now
        );
        LamdbdaManaged lamdbdaManaged = new DefaultLamdbdaManaged(
            localLambda.getLambdaUri(),
            lambdaClientSettings,
            lambdaHandler
        );

        looper = lamdbdaManaged.looper();

        testExecutor.submit(looper);

        r = localLambda.r();
    }

    @Override
    public void close() {
        looper.close();
        looper = null;
        localLambda.close();
        localLambda = null;
        Stream.of(serverExec, lambdaExec, testExecutor)
            .forEach(ExecutorService::shutdown);
        serverExec = lambdaExec = testExecutor = null;
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
        return getClass().getSimpleName() + "[" + name + "@" + localLambda.getLambdaUri() + "]";
    }
}
