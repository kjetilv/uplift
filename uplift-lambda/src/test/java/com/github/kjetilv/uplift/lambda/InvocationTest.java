package com.github.kjetilv.uplift.lambda;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationTest {

    @Test
    void noneIsEmpty() {
        assertThat(Invocation.none("str", Instant.now()).isEmpty()).isTrue();
    }

    @Test
    void failedIsEmpty() {
        assertThat(Invocation.failed(new Throwable(), Instant.now()).isEmpty()).isTrue();
    }

    @Test
    void failedReqIsEmpty() {
        assertThat(Invocation.failed("sdf", new Throwable(), Instant.now()).isEmpty()).isTrue();
    }
}
