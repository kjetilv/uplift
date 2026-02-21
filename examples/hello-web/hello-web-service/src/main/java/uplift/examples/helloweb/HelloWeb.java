package uplift.examples.helloweb;

import module java.base;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import com.github.kjetilv.uplift.lambda.LambdaPayload;
import com.github.kjetilv.uplift.lambda.LambdaResult;

public final class HelloWeb implements LambdaHandler {

    @Override
    public LambdaResult handle(LambdaPayload lambdaPayload) {
        var name = path(lambdaPayload);
        var greeting = Greeter.greet(name);
        return LambdaResult.json(greeting);
    }

    private static String path(LambdaPayload lambdaPayload) {
        return Optional.ofNullable(lambdaPayload.path()).orElse("url with no path");
    }
}
