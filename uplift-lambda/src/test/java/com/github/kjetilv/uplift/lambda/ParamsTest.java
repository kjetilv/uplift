package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.lambda.Params;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParamsTest {

    @Test
    void betterWorks() {
        Assertions.assertThat(Params.param("foo/bar/godhelpus", "foo/bar/{zot}", "zot"))
            .hasValue("godhelpus");
        assertThat(Params.params("god/hel/pus", "{foo}/{bar}/{zot}", "foo", "bar", "zot"))
            .containsExactly("god", "hel", "pus");
    }
}
