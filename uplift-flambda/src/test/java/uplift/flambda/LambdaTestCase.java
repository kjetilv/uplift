package uplift.flambda;

import java.lang.reflect.Method;

import com.github.kjetilv.uplift.flambda.LambdaHarness;
import com.github.kjetilv.uplift.flambda.Reqs;
import com.github.kjetilv.uplift.lambda.LambdaHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

@SuppressWarnings("unused")
public abstract class LambdaTestCase {

    private LambdaHarness lambdaHarness;

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        lambdaHarness = lambdaHarness(testInfo);
    }

    @AfterEach
    public void afterEach() {
        lambdaHarness.close();
        lambdaHarness = null;
    }

    /**
     * This method can be overridden.
     *
     * @param testInfo Test info
     *
     * @return Lambda test harness
     */
    protected LambdaHarness lambdaHarness(TestInfo testInfo) {
        return new LambdaHarness(testName(testInfo), lambdaHandler());
    }

    /**
     * This method will be called if you don't override {@link #lambdaHarness(TestInfo)}.
     *
     * @return A new lambda handler
     */
    protected LambdaHandler lambdaHandler() {
        throw new UnsupportedOperationException("Override either this method or #lambdaTestHarness");
    }

    protected Reqs reqs() {
        return lambdaHarness.reqs();
    }

    private static String testName(TestInfo testInfo) {
        return testInfo.getTestMethod()
            .map(Method::getName)
            .orElseThrow(() ->
                new IllegalStateException("No test method found on " + testInfo));
    }
}
