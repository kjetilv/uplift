package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.flows.Name;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

final class Chains {

    static void assertChain(
        int chainLength,
        int batchSize,
        int items,
        PathFqs<byte[], String> pathFqs
    ) {
        List<CompletableFuture<Void>> chain = new ArrayList<>();

        var format = "foo-G%d.txt";
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var initialName = Name.of(format.formatted(0));

        for (var i = 1; i < chainLength; i++) {
            var finalI = i;

            var linkName = Name.of(format.formatted(i));
            var writer = pathFqs.writer(linkName);
            var precedingLinkName = Name.of(format.formatted(i - 1));

            pathFqs.init(linkName, precedingLinkName);

            chain.add(CompletableFuture.runAsync(
                () -> {
                    pathFqs.reader(precedingLinkName).batches(batchSize)
                        .forEach(lines ->
                            lines.forEach(line ->
                                writer.write(line + "-" + finalI)
                            ));
                    writer.close();
                },
                executor
            ));
        }

        chain.add(CompletableFuture.runAsync(
            () -> {
                try (var source = pathFqs.writer(initialName)) {
                    for (var j = 0; j < items; j++) {
                        source.write("G-0");
                    }
                }
            },
            executor
        ));

        chain.forEach(CompletableFuture::join);

        var line = "G-" + IntStream.range(0, chainLength)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining("-"));

        assertThat(pathFqs.reader(Name.of("foo-G" + (chainLength - 1) + ".txt")).stream())
            .isNotEmpty()
            .allSatisfy(l ->
                assertThat(l).isEqualTo(line));
    }

    static void assertSimpleWriteRead(PathFqs<?, String> pfq) {
        try (var w = pfq.writer(Name.of("foo.txt"))) {
            for (var i = 0; i < 10; i++) {
                w.write(List.of("foo" + i, "bar" + i));
            }
        }

        var puller = pfq.reader(Name.of("foo.txt"));
        for (var i = 0; i < 10; i++) {
            assertThat(puller.next()).isEqualTo("foo" + i);
            assertThat(puller.next()).isEqualTo("bar" + i);
        }
        assertThat(puller.next()).isNull();
    }

    private Chains() {

    }

    static final int INT = 9999;
}
