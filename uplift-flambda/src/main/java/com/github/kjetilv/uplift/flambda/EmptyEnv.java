package com.github.kjetilv.uplift.flambda;

import module java.base;
import module uplift.kernel;

@SuppressWarnings({"NewExceptionWithoutArguments", "unused"})
public class EmptyEnv implements Env {

    @Override
    public URI awsLambdaUri() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String awsLambdaRuntimeApi() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String accessKey(String profile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String secretKey(String profile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sessionToken() {
        throw new UnsupportedOperationException();
    }
}
