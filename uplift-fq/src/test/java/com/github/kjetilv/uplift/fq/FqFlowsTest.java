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
import java.util.stream.IntStream;

class FqFlowsTest {

    @Test
    void test(
        @TempDir Path tmp,
        TestInfo testInfo
    ) {

        var fqs = PathFqs.create(
            tmp,
            new ByteBufferStringFio(StandardCharsets.UTF_8),
            new ChannelBufferAccessProvider(),
            new Dimensions(1, 2, 5)
        );

        var name = testInfo.getTestMethod().orElseThrow().getName();

        var flows = FqFlows.create(name, fqs)
            .batchSize(10)
            .timeout(Duration.ofMinutes(1))
            .onException((flow, items, e) -> {
                System.err.println("Exception in FqFlowsTest: " + e.getMessage());
            });

        var configured = flows
            .fromSource("in1").with(items ->
                items.stream()
                    .map(i -> i + "in1")
                    .toList())
            .from("in1", "in2").with(items ->
                items.stream()
                    .map(i -> i + "in2")
                    .toList());

        var strings = IntStream.range(0, 100).mapToObj(String::valueOf);

        configured.feed(strings);
    }

}