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

    default String accessKey() {
        return accessKey(null);
    }

    String accessKey(String profile);

    default String secretKey() {
        return secretKey(null);
    }

    String secretKey(String profile);

    String sessionToken();

    String authorizationToken();

    String credentialsFullUri();
}
