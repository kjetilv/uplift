package com.github.kjetilv.uplift.kernel.io;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrintTest {

    @Test
    void semiSecrets() {
        assertThat(Print.semiSecret("a")).isEqualTo("*");
        assertThat(Print.semiSecret("ab")).isEqualTo("a*");
        assertThat(Print.semiSecret("abc")).isEqualTo("a**");
        assertThat(Print.semiSecret("abcd")).isEqualTo("a***");
        assertThat(Print.semiSecret("abcdef")).isEqualTo("ab***ef");
        assertThat(Print.semiSecret("abcdefgh")).isEqualTo("ab***gh");
        assertThat(Print.semiSecret("abcdefghi")).isEqualTo("abc***ghi");
    }
}
