package com.github.kjetilv.uplift.kernel;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import com.github.kjetilv.uplift.kernel.aws.DefaultEnv;

public interface Env {

    static Env actual() {
        return ENV.updateAndGet(env -> env == null ? new DefaultEnv() : env);
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

    AtomicReference<DefaultEnv> ENV = new AtomicReference<>();
}
