package com.github.kjetilv.uplift.kernel.aws;

import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.EnvLookup;

import java.net.URI;
import java.util.Optional;

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
    public String accessKey(String profile) {
        return Optional.ofNullable(
                EnvLookup.get("aws.accessKeyId", "AWS_ACCESS_KEY_ID")
            ).or(() ->
                AwsLookup.get(profile).map(AwsAuth::key))
            .orElseThrow(() ->
                new IllegalStateException("No access key found"));
    }

    @Override
    public String secretKey(String profile) {
        return Optional.ofNullable(
                EnvLookup.get("aws.secretAccessKey", "AWS_SECRET_ACCESS_KEY")
            ).or(() ->
                AwsLookup.get(profile).map(AwsAuth::secret))
            .orElseThrow(() ->
                new IllegalStateException("No secret key found"));
    }

    @Override
    public String sessionToken() {
        return EnvLookup.get(null, "AWS_SESSION_TOKEN");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
