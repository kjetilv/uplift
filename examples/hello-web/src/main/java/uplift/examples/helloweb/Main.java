package uplift.examples.helloweb;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import uplift.kernel.Env;
import uplift.kernel.Time;
import uplift.lambda.LambdaClientSettings;
import uplift.lambda.LambdaResult;
import uplift.lambda.LamdbdaManaged;

import static uplift.kernel.ManagedExecutors.executor;

public final class Main {

    public static void main(String[] args) {
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
            lambdaPayload -> new
                LambdaResult(
                200,
                null,
                "\"Hello, web!\"".getBytes(StandardCharsets.UTF_8),
                false
            )
        ).run();
    }
}
