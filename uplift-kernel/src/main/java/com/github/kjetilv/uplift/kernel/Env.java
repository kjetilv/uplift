package com.github.kjetilv.uplift.kernel;

import java.net.URI;

import com.github.kjetilv.uplift.kernel.aws.DefaultEnv;

@SuppressWarnings("unused")
public interface Env {

    static Env actual() {
        return new DefaultEnv();
    }

    URI awsLambdaUri();

    String awsLambdaRuntimeApi();

    String accessKey();

    String secretKey();

    String sessionToken();

    String authorizationToken();

    String credentialsFullUri();
}
