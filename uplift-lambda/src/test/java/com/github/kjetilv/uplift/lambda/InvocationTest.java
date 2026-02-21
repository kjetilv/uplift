package com.github.kjetilv.uplift.lambda;

import module java.base;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationTest {

    @Test
    void noneIsEmpty() {
        assertThat(Invocation.none(REQUEST, Instant.now()).empty()).isTrue();
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
