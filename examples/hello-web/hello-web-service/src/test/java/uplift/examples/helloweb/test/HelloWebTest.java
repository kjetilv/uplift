package uplift.examples.helloweb.test;

import java.lang.reflect.Method;
import java.net.http.HttpResponse;

import com.github.kjetilv.uplift.asynchttp.HttpChannelHandler;
import com.github.kjetilv.uplift.flambda.LambdaTestHarness;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import uplift.examples.helloweb.HelloWeb;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWebTest {

    private LambdaTestHarness lambdaTestHarness;

    @BeforeEach
    void setup(TestInfo testInfo) {
        this.lambdaTestHarness =
            new LambdaTestHarness(testName(testInfo), new HelloWeb());
    }

    @AfterEach
    void teardown() {
        if (lambdaTestHarness != null) {
            lambdaTestHarness.close();
        }
    }

    @Test
    void helloYou() {
        helloCall("you")
            .req("GET")
            .thenApply(HttpResponse::body)
            .thenAccept(body ->
                assertThat(body)
                    .isEqualTo(helloResponse("you")))
            .join();
    }

    private HttpChannelHandler.R helloCall(String you) {
        return lambdaTestHarness.r().path("/" + you);
    }

    private String testName(TestInfo testInfo) {
        return testInfo.getTestMethod()
            .map(Method::getName)
            .orElseGet(() ->
                getClass().getSimpleName());
    }

    private static String helloResponse(String name) {
        return "\"Hello, /" + name + "!\"";
    }
}
