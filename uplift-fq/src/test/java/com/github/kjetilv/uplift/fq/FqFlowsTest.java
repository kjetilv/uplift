package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.flows.FqFlows;
import com.github.kjetilv.uplift.fq.flows.Name;
import com.github.kjetilv.uplift.fq.io.ByteBufferStringFio;
import com.github.kjetilv.uplift.fq.io.BytesStringFio;
import com.github.kjetilv.uplift.fq.paths.AccessProviders;
import com.github.kjetilv.uplift.fq.paths.Dimensions;
import com.github.kjetilv.uplift.fq.paths.PathFqs;
import com.github.kjetilv.uplift.fq.paths.bytes.StreamAccessProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;

class FqFlowsTest {

    @Test
    void testBuffers(@TempDir Path tmp, TestInfo testInfo) {
        var fqs = PathFqs.create(
            tmp,
            new ByteBufferStringFio(),
            AccessProviders.channelBuffers(),
            new Dimensions(2, 3, 5)
        );
        test(
            tmp,
            "",
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
            new ByteBufferStringFio(),
            AccessProviders.channelBuffers(),
            new Dimensions(2, 3, 5)
        );
        test(
            tmp,
            "",
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
        var byteBufferChannelAccessProvider = AccessProviders.channelBytes((byte) '\n');
        var fqs = PathFqs.create(
            tmp,
            new BytesStringFio(StandardCharsets.UTF_8),
            byteBufferChannelAccessProvider,
            new Dimensions(2, 3, 5)
        );
        test(
            tmp,
            "",
            testInfo,
            fqs,
            110,
            100,
            10,
            25,
            List.of(10, 25)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testArrays(boolean compressed, @TempDir Path tmp, TestInfo testInfo) {
        var fqs = PathFqs.create(
            tmp,
            new BytesStringFio(StandardCharsets.UTF_8),
            new StreamAccessProvider(
                compressed,
                (path, _) ->
                    assertThat(tmp).isDirectoryContaining(path::equals)
            ),
            new Dimensions(2, 3, 5)
        );

        test(
            tmp,
            compressed ? ".gz" : "",
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
    void scaleTest(@TempDir Path tmp, TestInfo testInfo) {
        var name = Name.of(testInfo.getTestMethod().orElseThrow().getName());

        FqFlows.ErrorHandler<String> stringErrorHandler = (_, _, e) -> e.toString();
        FqFlows.Processor<String> check = items -> items;

        var fqs = PathFqs.create(
            tmp,
            new ByteBufferStringFio(),
            AccessProviders.channelBuffers(),
            new Dimensions(2, 3, 6)
        );

        var flows = flows(
            name,
            fqs,
            100,
            stringErrorHandler,
            check
        );

        var count = feed(flows, 100_000).join().count();
        assertThat(count).isEqualTo(100_000);
    }

    @Test
    void scaleTest2(@TempDir Path tmp, TestInfo testInfo) {
        var name = Name.of(testInfo.getTestMethod().orElseThrow().getName());

        FqFlows.ErrorHandler<String> stringErrorHandler = (_, _, e) -> e.toString();
        FqFlows.Processor<String> check = items -> items;

        var fqs = PathFqs.create(
            tmp,
            new ByteBufferStringFio(),
            AccessProviders.channelBuffers(),
            new Dimensions(2, 3, 6)
        );

        var flows = flows(
            name,
            fqs,
            100,
            stringErrorHandler,
            check
        );

        var strings = IntStream.range(0, 100_000).mapToObj(String::valueOf);
        try (var writer = fqs.writer(name)) {
            strings.forEach(writer::write);
        }
        var count = flows.feed().join().count();
        assertThat(count).isEqualTo(-1);
    }

    private static final String GLOB = "glob:**/";

    private static final String DONE = "done";

    @SuppressWarnings("SameParameterValue")
    private static <T> void test(
        Path tmp,
        String gz,
        TestInfo testInfo,
        PathFqs<T, String> fqs,
        int count,
        int firstSize,
        int lastSize,
        int batchSize,
        List<Integer> batchSizes
    ) {
        var name = Name.of(testInfo.getTestMethod().orElseThrow().getName());
        var exceptions = new ArrayList<Exception>();

        FqFlows.ErrorHandler<String> stringErrorHandler = (_, _, e) -> {
            exceptions.add(e);
            return e.toString();
        };

        FqFlows.Processor<String> check = items -> {
            assertThat(items.size()).isIn(batchSizes);
            return items;
        };

        var flows = flows(
            name, fqs,
            batchSize,
            stringErrorHandler,
            check
        );

        var feed = feed(flows, count);
        assertThat(feed.count()).isEqualTo(count);
        feed.join();

        assertThat(exceptions).isEmpty();

        contents(tmp, gz, "1", "in1", firstSize, lastSize);
        contents(tmp, gz, "2", "in1in2", firstSize, lastSize);
        contents(tmp, gz, "3", "in1in3", firstSize, lastSize);
        contents(tmp, gz, "4", "in1in2in4", firstSize, lastSize);
        contents(tmp, gz, "X", "inX", firstSize, lastSize);
    }

    private static FqFlows.Run feed(FqFlows<String> flows, int count) {
        var strings = IntStream.range(0, count).mapToObj(String::valueOf);
        return flows.feed(strings);
    }

    private static <T> FqFlows<String> flows(
        Name name, PathFqs<T, String> fqs,
        int batchSize,
        FqFlows.ErrorHandler<String> stringErrorHandler,
        FqFlows.Processor<String> check
    ) {
        return FqFlows.builder(name, fqs)
            .batchSize(batchSize)
            .timeout(Duration.ofMinutes(1))
            .onException(stringErrorHandler)

            .from("in1", "in2").with(
                check.andThen(items ->
                    items.map(add("in2"))
                ))
            .from("in2").to(() -> "in4").with(
                check.andThen(items ->
                    items.map(add("in4"))
                ))
            .from(() -> "in1").to("in3").with(
                check.andThen(items ->
                    items.map(add("in3"))
                ))

            .fromSource("in1").with(
                check.andThen(items ->
                    items.map(add("in1"))
                ))
            .fromSource().to("inX").with(
                check.andThen(items ->
                    items.map(add("inX"))
                ))
            .build();
    }

    private static Function<String, String> add(String str) {
        return i -> i + str;
    }

    private static void contents(Path tmp, String gz, String index, String suffix, int firstSize, int lastSize) {
        var first = format("in{0}-00000.in{0}{1}", index, gz);
        var last = format("in{0}-00100.in{0}{1}", index, gz);
        var notFound = format("in{0}-00200.in{0}{1}", index, gz);
        var dir = tmp.resolve("in" + index);

        assertThat(dir)
            .isDirectoryContaining(GLOB + DONE)
            .isDirectoryContaining(GLOB + first)
            .isDirectoryContaining(GLOB + last)
            .isDirectoryNotContaining(GLOB + notFound);
        if (gz.isEmpty()) {
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
}