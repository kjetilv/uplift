package com.github.kjetilv.uplift.json.trie;

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

        assertThat(q).isEqualTo(ct.get(c("q"), 0, 1));
        assertThat(foo).isEqualTo(ct.get(c("foo"), 0, 3));
        assertThat(qoz).isEqualTo(ct.get(c("qoz"), 0, 3));
        assertThat(bar).isEqualTo(ct.get(c("bar"), 0, 3));
        assertThat(baz).isEqualTo(ct.get(c("baz"), 0, 3));
        assertThat(biz).isEqualTo(ct.get(c("biz"), 0, 3));
        assertThat(bizz).isEqualTo(ct.get(c("bizz"), 0, 4));
        assertThat(goobarzot).isEqualTo(ct.get(c("goobarzot"), 0, 9));
        assertThat(goobarzip).isEqualTo(ct.get(c("goobarzip"), 0, 9));
        assertThat(goozotzot).isEqualTo(ct.get(c("goozotzot"), 0, 9));
        assertThat(goozotzip).isEqualTo(ct.get(c("goozotzip"), 0, 9));
        assertThat(goozotzix).isEqualTo(ct.get(c("goozotzix"), 0, 9));
    }

    private static Token.Field tok(String qoz) {
        return new Token.Field(c(qoz));
    }

    private static byte[] c(String s) {
        return s.getBytes(UTF_8);
    }
}