package com.github.kjetilv.uplift.jmh;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.uplift.json.ffm.MemorySegmentJsonReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Threads;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class ReadBenchmark {

    @Fork(value = 0, warmups = 2)
    @Threads(8)
    @Benchmark
    public Tweet readTweetUplift() {
        return reader.read(lineSegment);
    }

    @Benchmark
    public Tweet readTweetJakcson() throws IOException {
        return objectMapper.readValue(data, Tweet.class);
    }

    public static final ObjectMapper objectMapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final URL RESOURCE = Objects.requireNonNull(
        Thread.currentThread().getContextClassLoader().getResource("48.json"), "resource");

    private static byte[] data;

    private static LineSegment lineSegment;

    private static MemorySegmentJsonReader<Tweet> reader;


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
        reader = new MemorySegmentJsonReader<>(TweetRW.INSTANCE.callbacks());
    }
}
