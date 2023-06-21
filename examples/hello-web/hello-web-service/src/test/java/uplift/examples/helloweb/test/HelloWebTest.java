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
        assertResponse("you");
    }

    @Test
    void helloStranger() {
        assertResponse(UUID.randomUUID().toString());
    }

    private void assertResponse(String someRando) {
        reqs().get("/" + someRando)
            .thenApply(HttpResponse::body)
            .thenAccept(body ->
                assertThat(body).isEqualTo(Greeter.greet("/" + someRando)))
            .join();
    }
}
