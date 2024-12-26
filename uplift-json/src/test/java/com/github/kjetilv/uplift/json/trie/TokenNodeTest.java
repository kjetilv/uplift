package com.github.kjetilv.uplift.json.trie;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenTrie;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class TokenNodeTest {

    @Test
    public void test() {
        Token.Field q = tok("q");
        Token.Field singular = tok("singular");

        Token.Field qoz = tok("qoz");
        Token.Field foo = tok("foo");
        Token.Field bar = tok("bar");
        Token.Field baz = tok("baz");
        Token.Field biz = tok("biz");
        Token.Field bizz = tok("bizz");

        Token.Field goobarzot = tok("goobarzot");
        Token.Field goobarzip = tok("goobarzip");

        Token.Field goozotzot = tok("goozotzot");
        Token.Field goozotzip = tok("goozotzip");
        Token.Field goozotzix = tok("goozotzix");

        TokenTrie ct = new TokenTrie(
            q,
            singular,
            qoz,
            foo,
            bar,
            baz,
            biz,
            bizz,
            goobarzot,
            goobarzip,
            goozotzip,
            goozotzot,
            goozotzix
        );

        assertThat(q).isEqualTo(ct.get(c("q")));
        assertThat(foo).isEqualTo(ct.get(c("foo")));
        assertThat(qoz).isEqualTo(ct.get(c("qoz")));
        assertThat(bar).isEqualTo(ct.get(c("bar")));
        assertThat(baz).isEqualTo(ct.get(c("baz")));
        assertThat(biz).isEqualTo(ct.get(c("biz")));
        assertThat(bizz).isEqualTo(ct.get(c("bizz")));
        assertThat(goobarzot).isEqualTo(ct.get(c("goobarzot")));
        assertThat(goobarzip).isEqualTo(ct.get(c("goobarzip")));
        assertThat(goozotzot).isEqualTo(ct.get(c("goozotzot")));
        assertThat(goozotzip).isEqualTo(ct.get(c("goozotzip")));
        assertThat(goozotzix).isEqualTo(ct.get(c("goozotzix")));
    }

    private static Token.Field tok(String qoz) {
        return new Token.Field(c(qoz));
    }

    private static LineSegment c(String s) {
        return LineSegments.of(s.getBytes(UTF_8));
    }
}