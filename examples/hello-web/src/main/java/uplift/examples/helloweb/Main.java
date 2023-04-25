package uplift.examples.helloweb;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.Time;
import com.github.kjetilv.uplift.lambda.LambdaClientSettings;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import com.github.kjetilv.uplift.lambda.LambdaResult;
import com.github.kjetilv.uplift.lambda.LamdbdaManaged;

import static com.github.kjetilv.uplift.kernel.ManagedExecutors.executor;

public final class Main {

    public static void main(String[] args) {
        LambdaHandler lambdaHandler = lambdaPayload -> new
                LambdaResult(
                200,
                null,
                "\"Hello, web!\"".getBytes(StandardCharsets.UTF_8),
                false
        );
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
}
