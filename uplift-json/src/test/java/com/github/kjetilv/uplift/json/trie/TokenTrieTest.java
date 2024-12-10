package com.github.kjetilv.uplift.json.trie;

import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenTrie;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenTrieTest {

    @Test
    public void test() {
        Token.Field q = tok("q");
        Token.Field singular = tok("singular");

        Token.Field qoz = tok("qoz");
        Token.Field foo = tok("foo");
        Token.Field bar = tok("bar");
        Token.Field baz = tok("baz");
        Token.Field biz = tok("biz");

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
            goobarzot,
            goobarzip,
            goozotzip,
            goozotzot,
            goozotzix
        );

        assertThat(q).isSameAs(ct.get(c("q")));
        assertThat(foo).isSameAs(ct.get(c("foo")));
        assertThat(qoz).isSameAs(ct.get(c("qoz")));
        assertThat(bar).isSameAs(ct.get(c("bar")));
        assertThat(baz).isSameAs(ct.get(c("baz")));
        assertThat(goobarzot).isSameAs(ct.get(c("goobarzot")));
        assertThat(goobarzip).isSameAs(ct.get(c("goobarzip")));
        assertThat(goozotzot).isSameAs(ct.get(c("goozotzot")));
        assertThat(goozotzip).isSameAs(ct.get(c("goozotzip")));
        assertThat(goozotzix).isSameAs(ct.get(c("goozotzix")));
    }

    private static Token.Field tok(String qoz) {
        return new Token.Field(c(qoz));
    }

    private static char[] c(String s) {
        return s.toCharArray();
    }
}