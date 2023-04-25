package com.github.kjetilv.uplift.lambda;

@FunctionalInterface
public interface LambdaHandler {

    LambdaResult handle(LambdaPayload lambdaPayload);
}
