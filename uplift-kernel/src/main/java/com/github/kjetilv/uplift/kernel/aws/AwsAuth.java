package com.github.kjetilv.uplift.kernel.aws;

import module java.base;

public record AwsAuth(String key, String secret) {

    public AwsAuth {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(secret, "secret");
    }
}
