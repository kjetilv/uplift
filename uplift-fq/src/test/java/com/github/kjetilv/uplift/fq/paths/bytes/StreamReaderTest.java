package com.github.kjetilv.uplift.fq.paths.bytes;

import module java.base;
import com.github.kjetilv.uplift.fq.paths.Reader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class StreamReaderTest {

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
        for (var i = body.length + 1; i >= 5; i--) {
            var puller = new StreamReader(tmp, new ByteArrayInputStream(body), i);
            try {
                assertString(puller, "foo", i);
                assertString(puller, "bar", i);
                assertString(puller, "zot", i);
                assertString(puller, "zip", i);
                assertString(puller, "xx", i);
                assertString(puller, "x", i);
                assertThat(puller.read()).isNull();
            } catch (Throwable e) {
                fail("Failed with buffer size " + i, e);
            }
        }
    }

    private static void assertString(Reader<byte[]> reader, String foo, int bs) {
        assertThat(reader.read())
            .describedAs("Could not get string %s with buffer size %s", foo, bs)
            .isEqualTo(foo.getBytes());
    }
}