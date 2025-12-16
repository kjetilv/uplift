package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.paths.bytes.StreamPuller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class StreamPullerTest {

    @Test
    void test(@TempDir(cleanup = CleanupMode.ALWAYS) Path tmp) {
        var body =
            """
                foo
                bar
                zot
                zip
                xx
                x
                """.getBytes();
        for (int i = 25; i >= 5; i--) {
            var puller = new StreamPuller(tmp, new ByteArrayInputStream(body), i);
            try {
                assertString(puller, "foo", i);
                assertString(puller, "bar", i);
                assertString(puller, "zot", i);
                assertString(puller, "zip", i);
                assertString(puller, "xx", i);
                assertString(puller, "x", i);
                assertThat(puller.pull()).isNull();
            } catch (Throwable e) {
                fail("Failed with buffer size " + i, e);
            }
        }
    }

    private static void assertString(Puller<byte[]> puller, String foo, int bs) {
        assertThat(puller.pull())
            .describedAs("Could not get string %s with buffer size %s", foo, bs)
            .isEqualTo(foo.getBytes());
    }
}