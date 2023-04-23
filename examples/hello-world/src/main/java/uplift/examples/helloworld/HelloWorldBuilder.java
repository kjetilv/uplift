package uplift.examples.helloworld;

import java.util.List;
import java.util.function.Consumer;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionUrl;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlCorsOptions;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.Runtime;

import static software.amazon.awscdk.services.lambda.Architecture.ARM_64;
import static software.amazon.awscdk.services.lambda.HttpMethod.GET;
import static software.amazon.awscdk.services.logs.RetentionDays.ONE_DAY;

@SuppressWarnings("unused")
public class HelloWorldBuilder implements Consumer<Stack> {

    @Override
    public void accept(Stack stack) {
        IFunction helloWorldFunction = Function.Builder.create(stack, "uplift-hello-world")
            .functionName("uplift-hello-world-fun")
            .code(Code.fromAsset("/lambdas/hello-world.zip"))
            .handler("bootstrap")
            .logRetention(ONE_DAY)
            .architecture(ARM_64)
            .memorySize(128)
            .runtime(Runtime.PROVIDED_AL2)
            .timeout(Duration.seconds(20))
            .build();

        FunctionUrl.Builder.create(stack, "uplift-hello-world-fun-url")
            .function(helloWorldFunction)
            .authType(FunctionUrlAuthType.NONE)
            .cors(FunctionUrlCorsOptions.builder()
                .allowedMethods(List.of(GET))
                .allowedOrigins(List.of("https://localhost:8080"))
                .allowedHeaders(List.of("Content-Type"))
                .maxAge(Duration.days(1))
                .allowCredentials(true)
                .build())
            .build();
    }
}
