package com.github.kjetilv.uplift.json.ffm;

import com.github.kjetilv.flopp.kernel.PartitionStreamer;
import com.github.kjetilv.flopp.kernel.Partitioned;
import com.github.kjetilv.flopp.kernel.files.PartitionedPaths;
import com.github.kjetilv.flopp.kernel.partitions.Partitioning;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class FastParseTest {

    @Test
    void parse() throws URISyntaxException, MalformedURLException {
        MemorySegmentJsonReader<Foo> foo = new MemorySegmentJsonReader<>(FooRW.INSTANCE.callbacks());
        Class<Foo> fooClass = Foo.class;
        System.out.println(fooClass);
        long sum;
        try (Partitioned<Path> partitioned = PartitionedPaths.partitioned(path(), Partitioning.create(4))) {
            sum = partitioned.streamers()
                .map(supplier(foo))
                .map(supplier ->
                    CompletableFuture.supplyAsync(supplier, EXECUTOR))
                .toList()
                .stream()
                .mapToLong(CompletableFuture::join)
                .sum();
        }
        System.out.println(sum);
    }

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private static Function<PartitionStreamer, Supplier<Long>> supplier(MemorySegmentJsonReader<Foo> foo) {
        return streamer ->
            () ->
                sum(streamer, foo);
    }

    private static long sum(PartitionStreamer streamer, MemorySegmentJsonReader<Foo> foo) {
        return streamer.lines()
            .map(foo::read)
            .mapToLong(Foo::foo)
            .sum();
    }

    private static URL uri() {
        return Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("lines.jsonl"));
    }

    private static Path path() throws MalformedURLException, URISyntaxException {
        return Path.of(uri().toURI().toURL().getPath());
    }
}