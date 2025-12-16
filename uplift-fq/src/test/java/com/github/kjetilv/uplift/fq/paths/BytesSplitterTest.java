package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.paths.bytes.BytesSplitter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class BytesSplitterTest {

    @Test
    void endLine() {
        assertLines(
            """
                x
                
                """);
    }

    @Test
    void simple() {
        assertLines("""
            
            foofoofoofoo
            foofoofoofo
            foofoofoof
            foofoofoo
            
            foofoofo
            foofoof
            foofoo
            
            
            
            foofo
            foof
            foo
            1
            1
            22
            bar
            1
            22
            333
            4444
            55555
            zot
            zip
            xx
            x
            
            
            """);
    }

    private static void assertLines(String string) {
        var body = string.getBytes();
        int minBuf = Arrays.stream(string.split("\n"))
                         .max(Comparator.comparing(String::length))
                         .map(String::length)
                         .orElseThrow() + 1;
        var maxBuf = body.length + 1;
        var strings = lines(string);
        for (int i = minBuf; i < maxBuf; i++) {
            try (var inputStream = new ByteArrayInputStream(body)) {
                var puller = new BytesSplitter(inputStream, (byte) '\n', i);
                try {
                    int finalI = i;
                    Arrays.stream(strings)
                        .forEach(line ->
                            assertThat(puller.next()).isNotNull()
                                .satisfies(bytes -> {
                                    var next = new String(bytes);
                                    assertThat(next)
                                        .describedAs("Could not get string %s with buffer size %s", line, finalI)
                                        .isEqualTo(line);
                                }));
                    assertThat(puller.next()).isNull();
                } catch (Throwable e) {
                    fail("Failed with buffer size " + i + " (" + minBuf + "-" + maxBuf + ")", e);
                }
            } catch (Exception e) {
                fail("Failed to closeX", e);
            }
        }
    }

    private static String[] lines(String string) {
        return (string.endsWith("\n") ? string.substring(0, string.length() - 1) : string)
            .split("\n", -1);
    }

}