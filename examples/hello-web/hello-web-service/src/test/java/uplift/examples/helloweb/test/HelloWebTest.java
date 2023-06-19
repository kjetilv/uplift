package uplift.examples.helloweb.test;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.github.kjetilv.uplift.flambda.CorsSettings;
import com.github.kjetilv.uplift.flambda.EmptyEnv;
import com.github.kjetilv.uplift.flambda.LocalLambda;
import com.github.kjetilv.uplift.flambda.LocalLambdaSettings;
import com.github.kjetilv.uplift.lambda.DefaultLamdbdaManaged;
import com.github.kjetilv.uplift.lambda.LambdaClientSettings;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import com.github.kjetilv.uplift.lambda.LambdaLooper;
import com.github.kjetilv.uplift.lambda.LamdbdaManaged;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uplift.examples.helloweb.HelloWeb;

import static com.github.kjetilv.uplift.kernel.ManagedExecutors.executor;

class HelloWebTest {

    private ExecutorService testExecutor;

    private final AtomicReference<Instant> time = new AtomicReference<>();

    private LocalLambda localLambda;

    private ExecutorService lambdaExec;

    private ExecutorService serverExec;

    private LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper;

    @BeforeEach
    void setup() {
        testExecutor = executor("test", 4);
        serverExec = executor("aws-yell-S", 5);
        lambdaExec = executor("aws-yell-L", 5);

        CorsSettings cors = new CorsSettings(
            List.of("*"),
            List.of("GET"),
            List.of("content-type", "range")
        );
        localLambda = new LocalLambda(new LocalLambdaSettings(
            0,
            0,
            8 * 8192,
            10,
            lambdaExec,
            serverExec,
            cors,
            time::get
        ));

        LambdaHandler lambdaHandler = new HelloWeb();

        LambdaClientSettings lambdaClientSettings = new LambdaClientSettings(
            new EmptyEnv(),
            Duration.ofSeconds(10),
            lambdaExec,
            serverExec,
            time::get
        );
        LamdbdaManaged lamdbdaManaged = new DefaultLamdbdaManaged(
            localLambda.getLambdaUri(),
            lambdaClientSettings,
            lambdaHandler
        );

        looper = lamdbdaManaged.looper();

        testExecutor.submit(looper);
    }

    @Test
    void helloYou() {

    }

    @AfterEach
    void teardown() {
        looper.close();
        looper = null;
        localLambda.close();
        localLambda = null;
        Stream.of(serverExec, lambdaExec, testExecutor)
            .forEach(ExecutorService::shutdown);
        serverExec = lambdaExec = testExecutor = null;
    }
}
