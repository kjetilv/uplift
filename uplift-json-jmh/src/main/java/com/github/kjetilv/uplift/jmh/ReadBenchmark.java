package com.github.kjetilv.uplift.jmh;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kjetilv.flopp.kernel.*;
import com.github.kjetilv.flopp.kernel.files.PartitionedPaths;
import com.github.kjetilv.flopp.kernel.files.Partitioneds;
import com.github.kjetilv.flopp.kernel.partitions.Partitioning;
import com.github.kjetilv.uplift.json.JsonReader;
import org.openjdk.jmh.annotations.Fork;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ReadBenchmark {

    public static void main(String[] args) throws IOException {
        System.out.println("OK");

        List<String> lines = Files.readAllLines(PATH_L, UTF_8);

        Instant initTime = Instant.now();
        System.out.println(TweetRW.INSTANCE.callbacks() + " in " + Duration.between(initTime, Instant.now())
            .toMillis());

        Shape shape = Shape.of(PATH_L);
        MemorySegmentSource segmentSource = PartitionedPaths.fullMemorySegmentSource(PATH_L, shape);

        LongAdder longAdder = new LongAdder();
        try (
            Partitioned partitioned = Partitioneds.create(Partitioning.single(), shape, segmentSource)
        ) {
            for (int i = 0; i < X / 4; i++) {
                partitioned.streamers()
                    .forEach(streamer ->
                        streamer.lines()
                            .forEach(line -> {
                                Tweet tweet = reader.read(line);
                                longAdder.add(tweet == null ? 0 : 1);
                            }));
            }
        }

        Tweet upliftTweet = reader.read(lineSegment);
        Tweet jacksonTweet = objectMapper.readValue(data, Tweet.class);
        if (upliftTweet.equals(jacksonTweet)) {
            throw new IllegalStateException("Not the same!");
        }

        System.out.println("Warmed up");
        System.gc();

        Instant jacksonNow = Instant.now();

        for (int i = 0; i < X; i++) {
            for (String line : lines) {
                try {
                    Tweet tweet = objectMapper.readValue(line, Tweet.class);
                    longAdder.add(tweet == null ? 0 : 1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Duration jacksonTime = Duration.between(jacksonNow, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
        System.out.println("Jackson: " + longAdder + ":" + jacksonTime);

        Instant upliftNow = Instant.now();
        try (
            Partitioned partitioned = Partitioneds.create(Partitioning.single(), shape, segmentSource)
        ) {
            for (int i = 0; i < X; i++) {
                partitioned.streamers()
                    .forEach(streamer ->
                        streamer.lines()
                            .forEach(line -> {
                                Tweet tweet = reader.read(line);
                                longAdder.add(tweet == null ? 0 : 1);
                            }));
            }
        }
        Duration upliftTime = Duration.between(upliftNow, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
        System.out.println("Uplift:" + longAdder + ": " + upliftTime);
        System.gc();

        String perc = perc(jacksonTime, upliftTime);
        System.out.println("Jackson spent " + perc + "% of the time uplift did");
        String perc2 = perc(upliftTime, upliftTime.plus(jacksonTime));
        System.out.println("Uplift spent " + perc2 + "% of the total " + upliftTime.plus(jacksonTime));
    }

    @Fork(value = 2, warmups = 2)
//    @Threads(8)
//    @Benchmark
    public Tweet readTweetUplift() {
        return reader.read(lineSegment);
    }

    @Fork(value = 2, warmups = 2)
//    @Benchmark
    public Tweet readTweetJakcson() throws IOException {
        return objectMapper.readValue(data, Tweet.class);
    }

    public static final ObjectMapper objectMapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT)
        .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

    private static final URL RESOURCE = Objects.requireNonNull(
        Thread.currentThread().getContextClassLoader().getResource("48.json"), "resource");

    private static final URL RESOURCE_L = Objects.requireNonNull(
        Thread.currentThread().getContextClassLoader().getResource("48.jsonl"), "resource");

    private static final Path PATH_L = Paths.get(RESOURCE_L.getPath());

    private static final byte[] data;

    private static final byte[][] datas;

    private static final LineSegment lineSegment;

    private static final JsonReader<LineSegment, Tweet> reader;

    private static final JsonReader<byte[], Tweet> bReader;

    private static final int X = 50_000;

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
        try (
            ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            try (InputStream inputStream = RESOURCE_L.openStream()) {
                inputStream.transferTo(out);
            }
            List<String> stream = Arrays.stream(new String(out.toByteArray(), UTF_8)
                    .split("\n"))
                .toList();
            datas = stream.stream()
                .map(line -> line.getBytes(UTF_8))
                .toArray(byte[][]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lineSegment = LineSegments.of(data);
        bReader = TweetRW.INSTANCE.bytesReader();
        reader = TweetRW.INSTANCE.lineSegmentReader();
    }

    private static String perc(Duration par, Duration total) {
        return BigDecimal.valueOf(par.toMillis() * 100.0 / total.toMillis())
            .setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
