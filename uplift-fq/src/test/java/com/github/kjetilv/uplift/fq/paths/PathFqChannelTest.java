package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fq;
import com.github.kjetilv.uplift.fq.io.BytesStringFio;
import com.github.kjetilv.uplift.fq.paths.ffm.ByteBufferWriter;
import com.github.kjetilv.uplift.fq.paths.ffm.ChannelsAccessProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS;

class PathFqChannelTest {

    @Test
    void testWrite(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        var fooTxt = tmp.resolve("foo.txt");
        try (
            var writer = new PathFqWriter<>(
                fooTxt,
                new Dimensions(1, 2, 3),
                path -> {
                    try {
                        return new ByteBufferWriter(path, '\n');
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                new BytesStringFio(),
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
            var fs = "foo-%s.txt".formatted(str);
            pathAssert
                .describedAs("Checking for %s", fs)
                .isDirectoryContaining(path ->
                    path.getFileName().toString().startsWith(fs));
        }
    }

    @Test
    void testSimpleWriteAndReader(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        var pfq = new PathFqs<>(
            new BytesStringFio(),
            new PathProvider(tmp),
            new ChannelsAccessProvider(),
            new Dimensions(1, 2, 4)
        );

        Chains.assertSimpleWriteRead(pfq);
    }

    @Test
    void testWriteAndRead(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        var pfq = new PathFqs<>(
            new BytesStringFio(),
            new PathProvider(tmp),
            new ChannelsAccessProvider(),
            new Dimensions(1, 2, 4)
        );

        var expected = IntStream.range(0, Chains.INT).boxed()
            .map(String::valueOf)
            .toList();

        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var writer = CompletableFuture.supplyAsync(
            () -> {
                try (
                    var fqw = pfq.writer("foo.txt")
                ) {
                    for (var i = 0; i < Chains.INT; i++) {
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

                for (var i = 0; i < Chains.INT; i++) {
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
            25,
            50,
            5_000
        );
    }

    @Test
    void chain(@TempDir(cleanup = ON_SUCCESS) Path tmp) {
        assertChain(
            tmp,
            new Dimensions(1, 3, 5),
            10,
            10,
            9999
        );
    }

    private static void assertChain(
        Path tmp,
        Dimensions dimensions,
        int chainLength,
        int batchSize,
        int items
    ) {
        Chains.assertChain(
            chainLength,
            batchSize,
            items,
            new PathFqs<>(
                new BytesStringFio(),
                new PathProvider(tmp),
                new ChannelsAccessProvider(),
                dimensions
            )
        );
    }
}