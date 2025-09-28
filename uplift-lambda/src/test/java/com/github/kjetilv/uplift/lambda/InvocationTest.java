package com.github.kjetilv.uplift.lambda;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationTest {

    @Test
    void noneIsEmpty() {
        assertThat(Invocation.none("str", Instant.now()).empty()).isTrue();
    }

    @Test
    void fatalIsEmpty() {
        assertThat(Invocation.fatal(new Throwable(), Instant.now()).empty()).isTrue();
    }

    @Test
    void fatalReqIsEmpty() {
        assertThat(Invocation.failed("sdf", Instant.now(), new Throwable()).empty()).isTrue();
    }
}
