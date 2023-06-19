package uplift.examples.helloweb;

import java.util.Optional;

import com.github.kjetilv.uplift.lambda.Lambda;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import com.github.kjetilv.uplift.lambda.LambdaPayload;
import com.github.kjetilv.uplift.lambda.LambdaResult;

import static com.github.kjetilv.uplift.lambda.LambdaResult.json;

public final class HelloWeb implements LambdaHandler {

    public static void main(String[] args) {
        Lambda.simply(new HelloWeb());
    }

    @Override
    public LambdaResult handle(LambdaPayload lambdaPayload) {
        String name = Optional.ofNullable(lambdaPayload.path()).orElse("url with no path");
        return json("\"Hello, " + name + "!\"");
    }
}
