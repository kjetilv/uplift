package com.github.kjetilv.uplift.synchttp;

import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {

    @Test
    void indexOf() {
        var chars10 = "foo.foobar";
        var chars26 = "xxfoobarxxfoobarxxfoobarxx";
        var segment = MemorySegment.ofArray((chars10 + chars26 + ".foo.bar").getBytes(UTF_8));
        var i = Utils.indexOf((byte) '.', segment, chars10.length(), 32);
        assertThat(i).isEqualTo(chars10.length() + chars26.length());
    }
}