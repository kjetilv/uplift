package com.github.kjetilv.uplift.json.tokens;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringsTest {

    @Test
    void unquoteStart() {
        String quoted = """
            \\"foo\\"
            """.trim();
        assertThat(quoted).hasSize(7);
        assertThat(Strings.unquote(quoted)).isEqualTo("""
            "foo"
            """.trim());
    }

}