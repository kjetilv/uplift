package uplift.examples.helloweb;

import module java.base;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;

import static software.amazon.awscdk.services.lambda.Architecture.ARM_64;
import static software.amazon.awscdk.services.lambda.HttpMethod.GET;

@SuppressWarnings("unused")
public class HelloWebBuilder implements Consumer<Stack> {

    @Override
    public void accept(Stack stack) {
        IFunction helloWorldFunction = Function.Builder.create(stack, "uplift-hello-web")
            .functionName("uplift-hello-web-function")
            .code(Code.fromAsset("/lambdas/hello-web-service.zip"))
            .handler("bootstrap")
            .architecture(ARM_64)
            .memorySize(128)
            .runtime(Runtime.PROVIDED_AL2)
            .timeout(Duration.seconds(20))
            .build();

        FunctionUrl.Builder.create(stack, "uplift-hello-web-function-url")
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
