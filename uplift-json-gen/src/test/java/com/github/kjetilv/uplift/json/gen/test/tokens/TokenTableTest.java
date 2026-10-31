package com.github.kjetilv.uplift.json.gen.test.tokens;

import module java.base;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenResolver;
import com.github.kjetilv.uplift.json.gen.tokens.TokenTable;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class TokenTableTest {

    private static final List<String> NAMES = List.of(
        "q",
        "singular",
        "qoz",
        "foo",
        "bar",
        "baz",
        "biz",
        "bizz",
        "goobarzot",
        "goobarzip",
        "goozotzot",
        "goozotzip",
        "goozotzix"
    );

    @Test
    void findsCanonicalInstances() {
        var fields = fields(NAMES);
        var table = new TokenTable(fields);
        fields.forEach(field ->
            assertThat(table.get(bytes(field.value()))).isSameAs(field));
    }

    @Test
    void findsAtOffset() {
        var fields = fields(NAMES);
        var table = new TokenTable(fields);
        fields.forEach(field -> {
            var name = field.value();
            var buffer = bytes("{\"x\": 1, \"" + name + "\": 2}");
            var offset = 10;
            var length = name.getBytes(UTF_8).length;
            assertThat(table.get(buffer, offset, length)).isSameAs(field);
            assertThat(table.get(i -> buffer[i], offset, length)).isSameAs(field);
        });
    }

    @Test
    void rejectsUnknownNamesOfSameLength() {
        var table = new TokenTable(fields(NAMES));
        assertUnknown(table, "fxx", "bxr", "gXXbarzot", "goobarAAA", "goozotzia", "qxz", "sXngular", "x");
    }

    @Test
    void rejectsPrefixesAndExtensions() {
        var table = new TokenTable(fields(NAMES));
        assertUnknown(table, "", "b", "bi", "go", "goo", "goobar", "goozotzi", "fooo", "bizzz", "qq", "goobarzott");
    }

    @Test
    void emptyTable() {
        var table = new TokenTable();
        assertUnknown(table, "", "a", "foo");
    }

    @Test
    void singleField() {
        var foo = new Token.Field("foo");
        var table = new TokenTable(foo);
        assertThat(table.get(bytes("foo"))).isSameAs(foo);
        assertUnknown(table, "", "f", "fo", "fox", "xoo", "fooo");
    }

    @Test
    void emptyName() {
        var empty = new Token.Field("");
        var a = new Token.Field("a");
        var table = new TokenTable(empty, a);
        assertThat(table.get(bytes(""))).isSameAs(empty);
        assertThat(table.get(bytes("a"))).isSameAs(a);
        assertUnknown(table, "b", "aa");
    }

    @Test
    void duplicateFields() {
        var foo = new Token.Field("foo");
        var table = new TokenTable(foo, new Token.Field("foo"), new Token.Field("bar"));
        assertThat(table.get(bytes("foo"))).isEqualTo(foo);
        assertUnknown(table, "fox");
    }

    @Test
    void multiByteNames() {
        var fields = fields(List.of("blåbær", "blåbærsyltetøy", "ærfugl", "øl", "ål", "日本", "日本語"));
        var table = new TokenTable(fields);
        fields.forEach(field ->
            assertThat(table.get(bytes(field.value()))).isSameAs(field));
        assertUnknown(table, "blåbar", "øx", "日", "日本人", "al");
    }

    @Test
    void wideAndDeep() {
        var names = IntStream.range(0, 2000)
            .mapToObj(i -> "f" + Integer.toString(i * 7919, 36))
            .distinct()
            .toList();
        var fields = fields(names);
        var table = new TokenTable(fields);
        fields.forEach(field ->
            assertThat(table.get(bytes(field.value()))).isSameAs(field));
        names.forEach(name -> {
            assertThat(table.get(bytes(name + "_"))).isNull();
            assertThat(table.get(bytes("_" + name.substring(1)))).isNull();
        });
    }

    @Test
    void collidingSampledHashesUseFullHash() {
        // Same length, first, middle and last byte: all collide under the sampled hash
        var names = IntStream.range(0, 400)
            .mapToObj(i -> "a%02xb%02xc".formatted(i / 20, i % 20))
            .toList();
        var fields = fields(names);
        var table = new TokenTable(fields);
        assertThat(table.toString()).contains("full");
        fields.forEach(field ->
            assertThat(table.get(bytes(field.value()))).isSameAs(field));
        assertUnknown(table, "a99b99c", "a00b00d", "a00b0c");
    }

    @Test
    void ordinaryNamesUseSampledHash() {
        assertThat(new TokenTable(fields(NAMES)).toString()).contains("sampled");
    }

    @Test
    void defaultResolverRespectsOffset() {
        var buffer = bytes("xx\"foo\"");
        assertThat(TokenResolver.DEFAULT.get(buffer, 3, 3)).isEqualTo(new Token.Field("foo"));
        assertThat(TokenResolver.DEFAULT.get(i -> buffer[i], 3, 3)).isEqualTo(new Token.Field("foo"));
    }

    private static void assertUnknown(TokenTable table, String... names) {
        for (var name : names) {
            assertThat(table.get(bytes(name)))
                .describedAs("Unknown name `%s`", name)
                .isNull();
            var buffer = bytes("\"" + name + "\"");
            assertThat(table.get(buffer, 1, buffer.length - 2))
                .describedAs("Unknown name `%s` at offset", name)
                .isNull();
        }
    }

    private static List<Token.Field> fields(List<String> names) {
        return names.stream()
            .map(Token.Field::new)
            .toList();
    }

    private static byte[] bytes(String s) {
        return s.getBytes(UTF_8);
    }
}
