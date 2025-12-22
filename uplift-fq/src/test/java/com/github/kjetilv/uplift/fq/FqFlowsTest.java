package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.io.ByteBufferStringFio;
import com.github.kjetilv.uplift.fq.paths.Dimensions;
import com.github.kjetilv.uplift.fq.paths.PathFqs;
import com.github.kjetilv.uplift.fq.paths.ffm.ChannelBufferAccessProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;

class FqFlowsTest {

    public static final String GLOB = "glob:**/";

    @Test
    void test(@TempDir Path tmp, TestInfo testInfo) {

        var fqs = PathFqs.create(
            tmp,
            new ByteBufferStringFio(StandardCharsets.UTF_8),
            new ChannelBufferAccessProvider(),
            new Dimensions(2, 3, 5)
        );

        var name = testInfo.getTestMethod().orElseThrow().getName();

        var flows = FqFlows.create(name, fqs)
            .batchSize(10)
            .timeout(Duration.ofMinutes(1))
            .onException((_, _, e) ->
                System.err.println("Exception in FqFlowsTest: " + e.getMessage()));

        var configured = flows
            .fromSource("in1").with(items ->
                items.stream()
                    .map(i -> i + "in1")
                    .toList())
            .fromSource("inX").with(items ->
                items.stream()
                    .map(i -> i + "inX")
                    .toList())
            .from("in1", "in2").with(items ->
                items.stream()
                    .map(i -> i + "in2")
                    .toList())
            .from("in1", "in3").with(items ->
                items.stream()
                    .map(i -> i + "in3")
                    .toList());

        var strings = IntStream.range(0, 110).mapToObj(String::valueOf);

        configured.feed(strings);

        assertContents(tmp, "1", "in1");
        assertContents(tmp, "2", "in1in2");
        assertContents(tmp, "3", "in1in3");
        assertContents(tmp, "X", "inX");
    }

    private static void assertContents(Path tmp, String index, String suffix) {
        var first100 = format("in{0}-00000.in{0}", index);
        var last10 = format("in{0}-00100.in{0}", index);
        var notFound = format("in{0}-00200.in{0}", index);
        var dir = tmp.resolve("in" + index);

        assertThat(dir)
            .isDirectoryContaining(GLOB + "done")
            .isDirectoryContaining(GLOB + first100)
            .isDirectoryContaining(GLOB + last10)
            .isDirectoryNotContaining(GLOB + notFound);
        assertThat(dir.resolve(first100))
            .isRegularFile()
            .content()
            .hasLineCount(100);
        assertThat(dir.resolve(last10))
            .isRegularFile()
            .content()
            .hasLineCount(10);
        List.of(first100, last10)
            .forEach(file ->
                assertThat(dir.resolve(file))
                    .content()
                    .satisfies(content ->
                        assertThat(content.split("\n")).allSatisfy(line ->
                            assertThat(line).endsWith(suffix))));
    }

}