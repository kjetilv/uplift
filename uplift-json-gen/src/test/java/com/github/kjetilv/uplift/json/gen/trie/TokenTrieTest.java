package com.github.kjetilv.uplift.json.gen.trie;

import com.github.kjetilv.uplift.json.Token;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class TokenTrieTest {

    @Test
    public void test() {
        var q = tok("q");
        var singular = tok("singular");

        var qoz = tok("qoz");
        var foo = tok("foo");
        var bar = tok("bar");
        var baz = tok("baz");
        var biz = tok("biz");
        var bizz = tok("bizz");

        var goobarzot = tok("goobarzot");
        var goobarzip = tok("goobarzip");

        var goozotzot = tok("goozotzot");
        var goozotzip = tok("goozotzip");
        var goozotzix = tok("goozotzix");

        var ct = new TokenTrie(
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
        var pre = "foo: \"";
        var c = c(pre + qoz + "\"");
        var slice = new byte[qoz.length()];
        System.arraycopy(c, pre.length(), slice, 0, qoz.length());
        return new Token.Field(slice);
    }

    private static byte[] c(String s) {
        return s.getBytes(UTF_8);
    }
}