package com.github.kjetilv.uplift.jmh;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;
import com.github.kjetilv.uplift.json.JsonReader;
import com.github.kjetilv.uplift.json.ffm.MemorySegmentJsonReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

public class ReadBenchmark {

    public static void main(String[] args) throws IOException {
        System.out.println("OK");

        LongAdder longAdder = new LongAdder();
        for (int i = 0; i < X / 5; i++) {
            Tweet upliftTweet = bReader.read(data);
            Tweet jacksonTweet = objectMapper.readValue(data, Tweet.class);
//            if (upliftTweet.equals(jacksonTweet)) {
//                 throw new IllegalStateException("Not the same!");
//            }
        }
        System.out.println("Warmed up");

        System.gc();
        Instant jacksonNow = Instant.now();
        for (int i = 0; i < X; i++) {
            Tweet tweet = objectMapper.readValue(data, Tweet.class);
            longAdder.add(tweet.entities().user_mentions().size());
        }
        Duration jacksonTime = Duration.between(jacksonNow, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
        System.out.println("Jackson: " + longAdder + ":" + jacksonTime);

        Instant upliftNow = Instant.now();
        for (int i = 0; i < X; i++) {
            Tweet read = bReader.read(data);
            longAdder.add(read.entities().user_mentions().size());
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
    @Benchmark
    public Tweet readTweetUplift() {
        return reader.read(lineSegment);
    }

    @Fork(value = 2, warmups = 2)
    @Benchmark
    public Tweet readTweetJakcson() throws IOException {
        return objectMapper.readValue(data, Tweet.class);
    }

    public static final ObjectMapper objectMapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT)
        .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

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
        bReader = TweetRW.INSTANCE.bytesReader();
        reader = new MemorySegmentJsonReader<>(TweetRW.INSTANCE.callbacks());
    }

    private static String perc(Duration par, Duration total) {
        return BigDecimal.valueOf(par.toMillis() * 100.0 / total.toMillis())
            .setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
