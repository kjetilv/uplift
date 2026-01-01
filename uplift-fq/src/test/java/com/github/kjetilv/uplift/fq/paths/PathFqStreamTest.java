package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.flows.Name;
import com.github.kjetilv.uplift.fq.io.BytesStringFio;
import com.github.kjetilv.uplift.fq.paths.bytes.StreamAccessProvider;
import com.github.kjetilv.uplift.fq.paths.bytes.StreamWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;

class PathFqStreamTest {

    @Test
    void testWrite(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        var fooTxt = getPath(tmp);

        var pathAssert =
            assertThat(fooTxt)
                .exists()
                .isDirectory();
        for (var i = 0; i < 10; i++) {
            var str = String.format("%03d", i * 10);
            var fs = "foo-%s.txt".formatted(str);
            pathAssert
                .describedAs("Checking for %s", fs)
                .isDirectoryContaining(path ->
                    path.getFileName().toString().startsWith(fs));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testSimpleWriteAndReader(boolean compress, @TempDir(cleanup = ON_SUCCESS) Path tmp) {
        var pfq = new PathFqs<>(
            new BytesStringFio(),
            new PathProvider(tmp),
            new StreamAccessProvider(compress, null),
            new Dimensions(1, 2, 4)
        );
        Chains.assertSimpleWriteRead(pfq);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testWriteAndRead(boolean compress, @TempDir(cleanup = ON_SUCCESS) Path tmp) {
        var pfq = new PathFqs<>(
            new BytesStringFio(),
            new PathProvider(tmp),
            new StreamAccessProvider(compress, null),
            new Dimensions(1, 2, 4)
        );
        Name foo = () -> "foo.txt";
        pfq.init(foo);

        var expected = IntStream.range(0, INT).boxed()
            .map(String::valueOf)
            .toList();

        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var writer = CompletableFuture.supplyAsync(
            () -> {
                try (
                    var fqw = pfq.writer(foo)
                ) {
                    for (var i = 0; i < INT; i++) {
                        fqw.write(String.valueOf(i));
                    }
                    return fqw;
                }
            },
            executor
        );

        var puller = CompletableFuture.runAsync(
            () -> {
                var fqp = pfq.reader(foo);

                for (var i = 0; i < INT; i++) {
                    assertThat(fqp.next()).isEqualTo(String.valueOf(i));
                }
                assertThat(fqp.next()).isNull();
            },
            executor
        );

        var streamer = CompletableFuture.runAsync(
            () -> {
                var fqs = pfq.reader(foo)
                    .stream();
                assertThat(fqs).containsExactlyElementsOf(expected);
            }, executor
        );

        var batcher = CompletableFuture.runAsync(
            () -> {
                var fqb = pfq.reader(foo).batches(100);
                var actual = fqb.flatMap(List::stream);
                assertThat(actual).containsExactlyElementsOf(expected);
            }
        );

        Stream.of(writer, puller, batcher, streamer)
            .forEach(CompletableFuture::join);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void longChain(
        boolean gzipped,
        @TempDir(cleanup = ON_SUCCESS) Path tmp
    ) {
        assertChain(
            tmp,
            new Dimensions(2, 4, 10),
            25,
            50,
            5_000,
            gzipped
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void chain(
        boolean gzipped,
        @TempDir(cleanup = ON_SUCCESS) Path tmp
    ) {
        assertChain(
            tmp,
            new Dimensions(1, 3, 4),
            10,
            10,
            9999,
            gzipped
        );
    }

    private static final int INT = 9999;

    private static Path getPath(Path tmp) {
        var fooTxt = tmp.resolve("foo.txt");
        try (
            var writer = new PathFqWriter<>(
                fooTxt,
                new Dimensions(1, 2, 3),
                path -> {
                    try {
                        return new StreamWriter(Files.newOutputStream(path), (byte) '\n');
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                new BytesStringFio(),
                new PathTombstone(fooTxt.resolve("done"))
            )
        ) {
            for (var i = 0; i < 110; i++) {
                writer.write(String.valueOf(i));
            }
        }
        return fooTxt;
    }

    private static void assertChain(
        Path tmp,
        Dimensions dimensions,
        int chainLength,
        int batchSize,
        int items,
        boolean gzipped
    ) {
        Chains.assertChain(
            chainLength,
            batchSize,
            items,
            new PathFqs<>(
                new BytesStringFio(),
                new PathProvider(tmp),
                new StreamAccessProvider(gzipped, null),
                dimensions
            )
        );
    }
}