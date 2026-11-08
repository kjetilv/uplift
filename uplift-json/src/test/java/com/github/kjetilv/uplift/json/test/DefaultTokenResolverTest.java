package com.github.kjetilv.uplift.json.test;

import module java.base;
import com.github.kjetilv.uplift.json.TokenResolver;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/// The default resolver finds cached names by their first and last eight bytes and their length. Names that
/// share those, and differ only in between, must not be confused.
class DefaultTokenResolverTest {

    @Test
    void resolvesNamesThatDifferOnlyInTheMiddle() {
        IntStream.rangeClosed(0, 70).forEach(length ->
            IntStream.range(0, Math.max(1, length)).forEach(position -> {
                var name = "n".repeat(length);
                var other = length == 0 ? "" : name.substring(0, position) + "x" + name.substring(position + 1);
                assertResolves(name);
                assertResolves(other);
                assertResolves(name);
            }));
    }

    @Test
    void resolvesFromAnyOffset() {
        var bytes = "__{\"æøå-name\":1}".getBytes(UTF_8);
        var offset = 4;
        var length = "æøå-name".getBytes(UTF_8).length;
        var field = TokenResolver.DEFAULT.get(bytes, offset, length);
        assertThat(field.value()).isEqualTo("æøå-name");
    }

    @Test
    void reusesCachedFields() {
        var bytes = "some-field-name".getBytes(UTF_8);
        var first = TokenResolver.DEFAULT.get(bytes, 0, bytes.length);
        var second = TokenResolver.DEFAULT.get(bytes.clone(), 0, bytes.length);
        assertThat(second).isSameAs(first);
    }

    private static void assertResolves(String name) {
        var bytes = ("pre" + name + "post").getBytes(UTF_8);
        var expected = name.getBytes(UTF_8);
        var field = TokenResolver.DEFAULT.get(bytes, 3, expected.length);
        assertThat(field.value()).isEqualTo(name);
        assertThat(field.length()).isEqualTo(expected.length);
        assertThat(Arrays.copyOf(field.bytes(), field.length())).isEqualTo(expected);
        assertThat(field.hash()).isEqualTo(Arrays.hashCode(expected));
    }
}
