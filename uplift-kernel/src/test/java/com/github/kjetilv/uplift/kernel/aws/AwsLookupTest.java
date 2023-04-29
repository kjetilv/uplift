package com.github.kjetilv.uplift.kernel.aws;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AwsLookupTest {

    @Test
    void shouldFindDefault() {
        assertThat(AwsLookup.get("default")).isPresent();
    }
}
