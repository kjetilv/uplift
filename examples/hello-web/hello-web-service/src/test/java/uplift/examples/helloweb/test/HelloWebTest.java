package uplift.examples.helloweb.test;

import java.net.http.HttpResponse;
import java.util.UUID;

import com.github.kjetilv.uplift.flambda.LambdaTestCase;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import org.junit.jupiter.api.Test;
import uplift.examples.helloweb.Greeter;
import uplift.examples.helloweb.HelloWeb;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWebTest extends LambdaTestCase {

    @Override
    protected LambdaHandler lambdaHandler() {
        return new HelloWeb();
    }

    @Test
    void helloYou() {
        assertResponse("you", Greeter.greet("/" + "you"));
    }

    @Test
    void helloStranger() {
        String random = UUID.randomUUID().toString();
        assertResponse(random, Greeter.greet("/" + random));
    }

    private void assertResponse(String someRando, String greeting) {
        reqs().get("/" + someRando)
            .thenApply(HttpResponse::body)
            .thenAccept(body ->
                assertThat(body).isEqualTo(greeting))
            .join();
    }
}
