package uplift.examples.helloweb;

import java.util.Optional;

import com.github.kjetilv.uplift.lambda.Lambda;
import com.github.kjetilv.uplift.lambda.LambdaPayload;
import com.github.kjetilv.uplift.lambda.LambdaResult;

import static com.github.kjetilv.uplift.lambda.LambdaResult.json;

public final class HelloWeb {

    public static void main(String[] args) {
        Lambda.simply(HelloWeb::respond);
    }

    private static LambdaResult respond(LambdaPayload payload) {
        return json(
            "\"Hello, " + Optional.ofNullable(payload.path()).orElse("pathless") + "!\"");
    }
}
