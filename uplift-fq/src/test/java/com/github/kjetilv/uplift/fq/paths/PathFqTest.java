package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fq;
import com.github.kjetilv.uplift.fq.io.StringFio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;

class PathFqTest {

    @Test
    void testWrite(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        var fooTxt = tmp.resolve("foo.txt");
        try (
            var writer = new PathFqWriter<>(
                fooTxt,
                new Dimensions(1, 2, 3),
                path -> {
                    try {
                        return new StreamWriter(Files.newOutputStream(path));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                new StringFio(),
                new PathTombstone(fooTxt.resolve("done"))
            )
        ) {
            for (int i = 0; i < 110; i++) {
                writer.write(String.valueOf(i));
            }
        }

        var pathAssert =
            assertThat(fooTxt)
                .exists()
                .isDirectory();
        for (int i = 0; i < 10; i++) {
            var str = String.format("%03d", i * 10);
            var fs = "foo-%s.txt.gz".formatted(str);
            pathAssert
                .describedAs("Checking for %s", fs)
                .isDirectoryContaining(path ->
                    path.getFileName().toString().startsWith(fs));
        }
    }

    @Test
    void testSimpleWriteAndReader(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        PathFqs<String> pfq = new PathFqs<>(
            tmp,
            new StringFio(),
            new Dimensions(1, 2, 4)
        );

        try (var w = pfq.writer("foo.txt")) {
            w.write(List.of("foo", "bar"));
        }

        var puller = pfq.puller("foo.txt");
        assertThat(puller.next()).hasValue("foo");
        assertThat(puller.next()).hasValue("bar");
        assertThat(puller.next()).isEmpty();
    }

    @Test
    void testWriteAndRead(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        PathFqs<String> pfq = new PathFqs<>(
            tmp,
            new StringFio(),
            new Dimensions(1, 2, 4)
        );

        List<String> expected = IntStream.range(0, INT).boxed()
            .map(String::valueOf)
            .toList();

        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var writer = CompletableFuture.supplyAsync(
            () -> {
                try (
                    var fqw = pfq.writer("foo.txt")
                ) {
                    for (int i = 0; i < INT; i++) {
                        fqw.write(String.valueOf(i));
                    }
                    return fqw;
                }
            },
            executor
        );

        var puller = CompletableFuture.supplyAsync(
            () -> {
                var fqp = pfq.puller("foo.txt");

                for (int i = 0; i < INT; i++) {
                    assertThat(fqp.next()).hasValue(String.valueOf(i));
                }
                assertThat(fqp.next()).isEmpty();
                return fqp;
            },
            executor
        );

        var streamer = CompletableFuture.supplyAsync(
            () -> {
                var fqs = pfq.streamer("foo.txt");
                assertThat(fqs.read()).containsExactlyElementsOf(expected);
                return fqs;
            }, executor
        );

        var batcher = CompletableFuture.supplyAsync(
            () -> {
                var fqb = pfq.batcher("foo.txt", 100);
                assertThat(fqb.read().flatMap(List::stream)).containsExactlyElementsOf(expected);
                return fqb;
            }
        );

        Stream.of(writer, puller, batcher, streamer)
            .forEach(CompletableFuture::join);

        assertThat(writer)
            .isCompletedWithValueMatching(Fq::done)
            .isCompletedWithValueMatching(Fq::done);
        assertThat(puller)
            .isCompletedWithValueMatching(Fq::done)
            .isCompletedWithValueMatching(p -> p.next().isEmpty());
        assertThat(streamer)
            .isCompletedWithValueMatching(Fq::done);
        assertThat(batcher)
            .isCompletedWithValueMatching(Fq::done);
    }

    @Test
    void longChain(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        assertChain(
            tmp,
            new Dimensions(2, 4, 10),
            50,
            100,
            10_000
        );
    }

    @Test
    void chain(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        assertChain(
            tmp,
            new Dimensions(1, 3, 4),
            10,
            10,
            9999
        );
    }

    @SuppressWarnings("resource")
    private static void assertChain(Path tmp, Dimensions dimensions, int chainLength, int batchSize, int items) {
        PathFqs<String> pathFqs = new PathFqs<>(
            tmp,
            new StringFio(),
            dimensions
        );

        List<CompletableFuture<Void>> chain = new ArrayList<>();

        var format = "foo-G%d.txt";

        for (int i = 1; i < chainLength; i++) {
            var writer = pathFqs.writer(format.formatted(i));
            int finalI = i;
            var batcher = pathFqs.batcher(format.formatted(i - 1), batchSize);
            chain.add(CompletableFuture.runAsync(
                () -> {
                    batcher.read()
                        .forEach(lines ->
                            lines.forEach(line ->
                                writer.write(line + "-" + finalI)));
                    writer.close();
                },
                Executors.newVirtualThreadPerTaskExecutor()
            ));
        }

        chain.add(CompletableFuture.runAsync(
            () -> {
                try (var source = pathFqs.writer(format.formatted(0))) {
                    for (int j = 0; j < items; j++) {
                        source.write("G-0");
                    }

                }
            },
            Executors.newVirtualThreadPerTaskExecutor()
        ));

        chain.forEach(CompletableFuture::join);

        var line = "G-" + IntStream.range(0, chainLength)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining("-"));

        assertThat(pathFqs.streamer("foo-G" + (chainLength - 1) + ".txt").read())
            .isNotEmpty()
            .allSatisfy(l ->
                assertThat(l).isEqualTo(line));
    }

    private static final int INT = 9999;
}