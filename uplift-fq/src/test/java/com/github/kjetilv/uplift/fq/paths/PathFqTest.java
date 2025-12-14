package com.github.kjetilv.uplift.fq.paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;

class PathFqTest {

    @Test
    void testWrite(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        try (
            var writer = new PathFqWriter<>(
                tmp.resolve("foo.txt"),
                new Dimensions(1, 2, 3),
                new StringFio(),
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
    void testWriteAndRead(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        var path = tmp.resolve("foo.txt");

        PathFqs<String> pfq = new PathFqs<>(tmp, new StringFio(), new Dimensions(1, 2, 4));

        var writer = CompletableFuture.supplyAsync(() -> {
            try (
                var fqw = pfq.write("foo.txt")
            ) {
                for (int i = 0; i < INT; i++) {
                    fqw.write(String.valueOf(i));
                }
                return fqw;
            }
        });

        var puller = CompletableFuture.supplyAsync(() -> {
            var fqp = pfq.pull("foo.txt");

            for (int i = 0; i < INT; i++) {
                assertThat(fqp.next()).hasValue(String.valueOf(i));
            }
            assertThat(fqp.next()).isEmpty();
            return fqp;
        });

        var streamer = CompletableFuture.supplyAsync(() -> {
            var fqs = pfq.stream("foo.txt");

            List<String> expected = IntStream.range(0, INT).boxed().map(String::valueOf).toList();

            assertThat(fqs.read()).containsExactlyElementsOf(expected);
            return fqs;
        });

        assertThat(writer.join()).satisfies(w ->
            assertThat(w.done()).isTrue());
        assertThat(puller.join()).satisfies(p -> {
            assertThat(p.next()).isEmpty();
            assertThat(p.done()).isTrue();
        });
        assertThat(streamer.join()).satisfies(s ->
            assertThat(s.done()).isTrue());
    }

    private static final int INT = 9999;
}