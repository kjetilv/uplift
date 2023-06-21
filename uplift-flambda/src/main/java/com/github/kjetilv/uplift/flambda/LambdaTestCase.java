package com.github.kjetilv.uplift.flambda;

import java.lang.reflect.Method;

import com.github.kjetilv.uplift.lambda.LambdaHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public abstract class LambdaTestCase {

    private LambdaTestHarness lambdaTestHarness;

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        lambdaTestHarness = lambdaTestHarness(testInfo);
    }

    @AfterEach
    public void afterEach() {
        lambdaTestHarness.close();
        lambdaTestHarness = null;
    }

    /**
     * This method can be overridden.
     *
     * @param testInfo Test info
     *
     * @return Lambda test harness
     */
    protected LambdaTestHarness lambdaTestHarness(TestInfo testInfo) {
        return new LambdaTestHarness(testName(testInfo), lambdaHandler());
    }

    /**
     * This method will be called if you don't override {@link #lambdaTestHarness(TestInfo)}.
     *
     * @return A new lambda handler
     */
    @SuppressWarnings("MethodMayBeStatic")
    protected LambdaHandler lambdaHandler() {
        throw new UnsupportedOperationException("Override either this method or #lambdaTestHarness");
    }

    protected Reqs reqs() {
        return lambdaTestHarness.reqs();
    }

    private static String testName(TestInfo testInfo) {
        return testInfo.getTestMethod()
            .map(Method::getName)
            .orElseThrow(() ->
                new IllegalStateException("No test method found on " + testInfo));
    }
}
