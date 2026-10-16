package com.github.kjetilv.uplift.lambda.test;

import module java.base;
import com.github.kjetilv.uplift.lambda.Invocation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationTest {

    @Test
    void noneIsEmpty() {
        Assertions.assertThat(Invocation.none(REQUEST, Instant.now()).empty()).isTrue();
    }

    @Test
    void fatalIsEmpty() {
        assertThat(Invocation.fatal(new Throwable(), Instant.now()).empty()).isTrue();
    }

    @Test
    void fatalReqIsEmpty() {
        assertThat(Invocation.failed(REQUEST, Instant.now(), new Throwable()).empty()).isTrue();
    }

    private static final HttpRequest REQUEST = HttpRequest.newBuilder().uri(URI.create("http://foo")).build();
}
