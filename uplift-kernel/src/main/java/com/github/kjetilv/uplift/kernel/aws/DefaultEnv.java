package com.github.kjetilv.uplift.kernel.aws;

import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.EnvLookup;

import java.net.URI;

public final class DefaultEnv implements Env {

    @Override
    @SuppressWarnings("HttpUrlsUsage")
    public URI awsLambdaUri() {
        return URI.create("http://" + awsLambdaRuntimeApi() + "/2018-06-01/runtime/invocation/next");
    }

    @Override
    public String awsLambdaRuntimeApi() {
        return EnvLookup.getRequired("AWS_LAMBDA_RUNTIME_API");
    }

    @Override
    public String accessKey() {
        return EnvLookup.get("aws.accessKeyId", "AWS_ACCESS_KEY_ID", true);
    }

    @Override
    public String secretKey() {
        return EnvLookup.get("aws.secretAccessKey", "AWS_SECRET_ACCESS_KEY", true);
    }

    @Override
    public String sessionToken() {
        return EnvLookup.get(null, "AWS_SESSION_TOKEN");
    }

    @Override
    public String authorizationToken() {
        return EnvLookup.get(null, "AWS_CONTAINER_AUTHORIZATION_TOKEN");
    }

    @Override
    public String credentialsFullUri() {
        return EnvLookup.get(null, "AWS_CONTAINER_CREDENTIALS_FULL_URI");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
