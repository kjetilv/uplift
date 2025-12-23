package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.io.ByteBufferStringFio;
import com.github.kjetilv.uplift.fq.io.BytesStringFio;
import com.github.kjetilv.uplift.fq.paths.Dimensions;
import com.github.kjetilv.uplift.fq.paths.PathFqs;
import com.github.kjetilv.uplift.fq.paths.bytes.StreamAccessProvider;
import com.github.kjetilv.uplift.fq.paths.ffm.ChannelBufferAccessProvider;
import com.github.kjetilv.uplift.fq.paths.ffm.ChannelBytesAccessProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;

class FqFlowsTest {

    @Test
    void testBuffers(@TempDir Path tmp, TestInfo testInfo) {
        var fqs = PathFqs.create(
            tmp,
            new ByteBufferStringFio(StandardCharsets.UTF_8),
            new ChannelBufferAccessProvider(),
            new Dimensions(2, 3, 5)
        );
        test(
            tmp,
            testInfo,
            fqs,
            110,
            100,
            10,
            25,
            List.of(10, 25)
        );
    }

    @Test
    void testBuffersOtherSizes(@TempDir Path tmp, TestInfo testInfo) {
        var fqs = PathFqs.create(
            tmp,
            new ByteBufferStringFio(StandardCharsets.UTF_8),
            new ChannelBufferAccessProvider(),
            new Dimensions(2, 3, 5)
        );
        test(
            tmp,
            testInfo,
            fqs,
            110,
            100,
            10,
            60,
            List.of(60, 50)
        );
    }

    @Test
    void testChannelArrays(@TempDir Path tmp, TestInfo testInfo) {
        var fqs = PathFqs.create(
            tmp,
            new BytesStringFio(StandardCharsets.UTF_8),
            new ChannelBytesAccessProvider(),
            new Dimensions(2, 3, 5)
        );
        test(
            tmp,
            testInfo,
            fqs,
            110,
            100,
            10,
            25,
            List.of(10, 25)
        );
    }

    @Test
    void testArrays(@TempDir Path tmp, TestInfo testInfo) {
        var fqs = PathFqs.create(
            tmp,
            new BytesStringFio(StandardCharsets.UTF_8),
            new StreamAccessProvider(),
            new Dimensions(2, 3, 5)
        );

        test(
            tmp,
            testInfo,
            fqs,
            110,
            100,
            10,
            25,
            List.of(10, 25)
        );
    }

    private static final String GLOB = "glob:**/";

    private static final String DONE = "done";

    private static <T> void test(
        Path tmp,
        TestInfo testInfo,
        PathFqs<T, String> fqs,
        int count,
        int firstSize,
        int lastSize,
        int batchSize,
        List<Integer> batchSizes
    ) {
        var name = testInfo.getTestMethod().orElseThrow().getName();
        var ref = new AtomicReference<Exception>();
        UnaryOperator<List<String>> check = items -> {
            assertThat(items.size()).isIn(batchSizes);
            return items;
        };

        var configured = FqFlows.create(name, fqs, batchSize)
            .timeout(Duration.ofMinutes(1))
            .onException((_, _, e) -> ref.set(e))

            .fromSource("in1").with(items ->
                check.apply(items).stream()
                    .map(i -> i + "in1")
                    .toList())
            .fromSource("inX").with(items ->
                check.apply(items).stream()
                    .map(i -> i + "inX")
                    .toList())
            .from("in1", "in2").with(items ->
                check.apply(items).stream()
                    .map(i -> i + "in2")
                    .toList())
            .from("in2", "in4").with(items ->
                check.apply(items).stream()
                    .map(i -> i + "in4")
                    .toList())
            .from("in1", "in3").with(items ->
                check.apply(items).stream()
                    .map(i -> i + "in3")
                    .toList());

        var strings = IntStream.range(0, count).mapToObj(String::valueOf);

        configured.feed(strings);

        assertThat(ref).hasValue(null);

        contents(tmp, "1", "in1", firstSize, lastSize);
        contents(tmp, "2", "in1in2", firstSize, lastSize);
        contents(tmp, "3", "in1in3", firstSize, lastSize);
        contents(tmp, "4", "in1in2in4", firstSize, lastSize);
        contents(tmp, "X", "inX", firstSize, lastSize);
    }

    private static void contents(Path tmp, String index, String suffix, int firstSize, int lastSize) {
        var first = format("in{0}-00000.in{0}", index);
        var last = format("in{0}-00100.in{0}", index);
        var notFound = format("in{0}-00200.in{0}", index);
        var dir = tmp.resolve("in" + index);

        assertThat(dir)
            .isDirectoryContaining(GLOB + DONE)
            .isDirectoryContaining(GLOB + first)
            .isDirectoryContaining(GLOB + last)
            .isDirectoryNotContaining(GLOB + notFound);
        assertThat(dir.resolve(first))
            .isRegularFile()
            .content()
            .hasLineCount(firstSize);
        assertThat(dir.resolve(last))
            .isRegularFile()
            .content()
            .hasLineCount(lastSize);
        assertThat(dir.resolve(DONE))
            .isRegularFile()
            .content()
            .isNotBlank();
        List.of(first, last)
            .forEach(file ->
                assertThat(dir.resolve(file))
                    .content()
                    .satisfies(content ->
                        assertThat(content.split("\n")).allSatisfy(line ->
                            assertThat(line).endsWith(suffix))));
    }
}