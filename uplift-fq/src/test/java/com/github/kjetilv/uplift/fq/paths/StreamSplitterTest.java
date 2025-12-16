package com.github.kjetilv.uplift.fq.paths;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class StreamSplitterTest {

    @Test
    void simple() {
        var body =
            """
                foo
                1
                22
                bar
                zot
                zip
                xx
                x
                """.getBytes();
        for (int i = 30; i >= 4; i--) {
            var puller = new StreamSplitter(new ByteArrayInputStream(body), (byte) '\n', i);
            try {
                assertString(puller, "foo", i);
                assertString(puller, "1", i);
                assertString(puller, "22", i);
                assertString(puller, "bar", i);
                assertString(puller, "zot", i);
                assertString(puller, "zip", i);
                assertString(puller, "xx", i);
                assertString(puller, "x", i);
                assertThat(puller.next()).isNull();
            } catch (Throwable e) {
                fail("Failed with buffer size " + i, e);
            }
        }
    }

    private static void assertString(StreamSplitter puller, String foo, int bs) {
        assertThat(puller.next())
            .describedAs("Could not get string %s with buffer size %s", foo, bs)
            .isEqualTo(foo.getBytes());
    }
}