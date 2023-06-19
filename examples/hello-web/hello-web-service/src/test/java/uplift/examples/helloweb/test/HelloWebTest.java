package uplift.examples.helloweb.test;

import java.lang.reflect.Method;
import java.net.http.HttpResponse;

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
        String name = testName(testInfo);
        lambdaTestHarness = new LambdaTestHarness(name, new HelloWeb());
    }

    @AfterEach
    void teardown() {
        lambdaTestHarness.close();
    }

    @Test
    void helloYou() {
        lambdaTestHarness.r().path("/you").req("GET")
            .thenApply(HttpResponse::body)
            .thenAccept(body ->
                assertThat(body).isEqualTo("\"Hello, /you!\""))
            .join();
    }

    private String testName(TestInfo testInfo) {
        return testInfo.getTestMethod()
            .map(Method::getName)
            .orElseGet(() ->
                getClass().getSimpleName());
    }
}
