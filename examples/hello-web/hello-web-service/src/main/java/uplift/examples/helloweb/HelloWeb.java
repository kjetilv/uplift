package uplift.examples.helloweb;

import java.util.Optional;

import com.github.kjetilv.uplift.lambda.Lambda;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import com.github.kjetilv.uplift.lambda.LambdaPayload;
import com.github.kjetilv.uplift.lambda.LambdaResult;

public final class HelloWeb implements LambdaHandler {

    public static void main(String[] args) {
        Lambda.simply(new HelloWeb());
    }

    @Override
    public LambdaResult handle(LambdaPayload lambdaPayload) {
        String name = path(lambdaPayload);
        String greeting = Greeter.greet(name);
        return LambdaResult.json(greeting);
    }

    private static String path(LambdaPayload lambdaPayload) {
        return Optional.ofNullable(lambdaPayload.path()).orElse("url with no path");
    }
}
