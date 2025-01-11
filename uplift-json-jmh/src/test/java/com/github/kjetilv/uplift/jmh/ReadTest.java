package com.github.kjetilv.uplift.jmh;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.flopp.kernel.Partitioned;
import com.github.kjetilv.flopp.kernel.files.PartitionedPaths;
import com.github.kjetilv.flopp.kernel.partitions.Partitioning;
import com.github.kjetilv.uplift.json.JsonReader;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenTrie;
import com.github.kjetilv.uplift.json.events.LineSegmentJsonReader;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Threads;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadTest {

    @Fork(value = 2, warmups = 2)
    @Threads(8)
    @Benchmark
    public Tweet readTweetUplift() {
        JsonReader<LineSegment, Tweet> reader1 = TweetRW.INSTANCE.lineSegmentReader();
        return reader1.read(LineSegments.of(data));
    }

    @Test
    void read() throws IOException {
        Tweet read1 = bReader.read(data);
        assertThat(read1).isNotNull();

        Tweet read2 = reader.read(lineSegment);
        assertThat(read2).isNotNull();

        Tweet read3 = objectMapper.readValue(data, Tweet.class);
        assertThat(read3).isNotNull();

//        assertThat(read2).isEqualTo(read3);
//        assertThat(read1).isEqualTo(read3);

        TokenTrie tokenTrie = Tweet_Callbacks.PRESETS.getTokenTrie();
        Token.Field resolve1 = tokenTrie.get("retweeters_count");
        Token.Field resolve2 = tokenTrie.get("retweeters_count");
        assertThat(resolve1).isNotNull().isSameAs(resolve2);
//
    }

    @Test
    void readFile() {
        try (Partitioned partitioned = PartitionedPaths.partitioned(PATH_L, Partitioning.single())) {
            partitioned.streamers()
                .forEach(streamer ->
                    streamer.lines()
                        .forEach(line -> {
                            Tweet tweet = reader.read(line);
                            assertThat(tweet).isNotNull();
                        }));
        }
    }

    public static final ObjectMapper objectMapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT)
        .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

    private static final URL RESOURCE_L = Objects.requireNonNull(
        Thread.currentThread().getContextClassLoader().getResource("48.jsonl"), "resource");

    private static final Path PATH_L = Paths.get(RESOURCE_L.getPath());

    private static final URL RESOURCE = Objects.requireNonNull(
        Thread.currentThread().getContextClassLoader().getResource("48.json"), "resource");

    private static final byte[] data;

    private static final LineSegment lineSegment;

    private static final JsonReader<LineSegment, Tweet> reader;

    private static final JsonReader<byte[], Tweet> bReader;

    private static final int X = 2_000_000;

    static {
        try (
            ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            try (InputStream inputStream = RESOURCE.openStream()) {
                inputStream.transferTo(out);
            }
            data = out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lineSegment = LineSegments.of(data);
        System.out.println(Tweet_Callbacks.PRESETS);
        bReader = TweetRW.INSTANCE.bytesReader();
        reader = new LineSegmentJsonReader<>(TweetRW.INSTANCE.callbacks());
    }
}
