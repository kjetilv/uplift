package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fq;
import com.github.kjetilv.uplift.fq.FqPuller;
import com.github.kjetilv.uplift.fq.FqWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;

class PathFqTest {

    @Test
    void testWrite(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        try (
            var writer = new PathFqWriter<>(
                tmp.resolve("foo.txt"),
                new Dimensions(1, 2, 3),
                new com.github.kjetilv.uplift.fq.io.StringFio(),
                StandardCharsets.UTF_8
            )
        ) {
            for (int i = 0; i < 110; i++) {
                writer.write(String.valueOf(i));
            }
        }

        var pathAssert =
            assertThat(tmp.resolve("foo.txt"))
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
    void testSimpleWriteAndReade(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        PathFqs<String> pfq = new PathFqs<>(
            tmp,
            new com.github.kjetilv.uplift.fq.io.StringFio(),
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
            new com.github.kjetilv.uplift.fq.io.StringFio(),
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

    @SuppressWarnings("resource")
    @Test
    void chain(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        PathFqs<String> pathFqs = new PathFqs<>(
            tmp,
            new com.github.kjetilv.uplift.fq.io.StringFio(),
            new Dimensions(1, 3, 4)
        );

        List<CompletableFuture<Void>> chain = new ArrayList<>();

        var format = "foo-G%d.txt";

        for (int i = 1; i < 10; i++) {
            var writer = pathFqs.writer(format.formatted(i));
            int finalI = i;
            var batcher = pathFqs.batcher(format.formatted(i - 1), 10);
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
                    for (int j = 0; j < INT; j++) {
                        source.write("G-0");
                    }

                }
            },
            Executors.newVirtualThreadPerTaskExecutor()
        ));

        chain.forEach(CompletableFuture::join);

        assertThat(pathFqs.streamer("foo-G9.txt").read())
            .isNotEmpty()
            .allSatisfy(line ->
                assertThat(line).isEqualTo("G-0-1-2-3-4-5-6-7-8-9"));
    }

    private static final int INT = 9999;
}