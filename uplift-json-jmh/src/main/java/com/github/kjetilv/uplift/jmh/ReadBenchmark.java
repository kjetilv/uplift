package com.github.kjetilv.uplift.jmh;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kjetilv.uplift.json.JsonReader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

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

@Fork
@Warmup(iterations = 1, time = 10)
@Measurement(iterations = 1, time = 10)
public class ReadBenchmark {

    public static final ObjectMapper objectMapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .setDefaultPropertyInclusion(JsonInclude.Include.NON_DEFAULT)
        .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

    static void main() throws IOException {
        System.out.println("OK");

        var lines = Files.readAllLines(PATH_L, UTF_8);

        var initTime = Instant.now();
        System.out.println(TweetRW.INSTANCE.callbacks() + " in " + Duration.between(initTime, Instant.now())
            .toMillis());

        var longAdder = new LongAdder();
        doUplift(lines, longAdder, 4);
        doJackson(lines, longAdder, 4);

        System.out.println("Warmed up");
        System.gc();

        var jacksonNow = Instant.now();

        doJackson(lines, longAdder, 1);
        var jacksonTime = Duration.between(jacksonNow, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
        System.out.println("Jackson: " + longAdder + ":" + jacksonTime);

        var upliftNow = Instant.now();
        doUplift(lines, longAdder, 1);
        var upliftTime = Duration.between(upliftNow, Instant.now()).truncatedTo(ChronoUnit.MILLIS);
        System.out.println("Uplift:" + longAdder + ": " + upliftTime);
        System.gc();

        var perc = perc(jacksonTime, upliftTime);
        System.out.println("Jackson spent " + perc + "% of the time uplift did");
        var perc2 = perc(upliftTime, upliftTime.plus(jacksonTime));
        System.out.println("Uplift spent " + perc2 + "% of the total " + upliftTime.plus(jacksonTime));
    }

    @Benchmark
    public Tweet readTweetUplift() {
        return bReader.read(data);
    }

    @Benchmark
    public Tweet readTweetJakcson() throws IOException {
        return objectMapper.readValue(data, Tweet.class);
    }

    private static final URL RESOURCE = Objects.requireNonNull(
        Thread.currentThread().getContextClassLoader().getResource("48.json"), "resource");

    private static final URL RESOURCE_L = Objects.requireNonNull(
        Thread.currentThread().getContextClassLoader().getResource("48.jsonl"), "resource");

    private static final Path PATH_L = Paths.get(RESOURCE_L.getPath());

    private static final byte[] data;

    private static final byte[][] datas;

    private static final JsonReader<String, Tweet> reader;

    private static final JsonReader<byte[], Tweet> bReader;

    private static final int X = 50_000;

    static {
        try (
            var out = new ByteArrayOutputStream()
        ) {
            try (var inputStream = RESOURCE.openStream()) {
                inputStream.transferTo(out);
            }
            data = out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (
            var out = new ByteArrayOutputStream()
        ) {
            try (var inputStream = RESOURCE_L.openStream()) {
                inputStream.transferTo(out);
            }
            var stream = Arrays.stream(new String(out.toByteArray(), UTF_8)
                    .split("\n"))
                .toList();
            datas = stream.stream()
                .map(line -> line.getBytes(UTF_8))
                .toArray(byte[][]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bReader = TweetRW.INSTANCE.bytesReader();
        reader = TweetRW.INSTANCE.stringReader();
    }

    private static void doUplift(List<String> lines, LongAdder longAdder, int split) {
        for (var i = 0; i < X / split; i++) {
            for (var line : lines) {
                var tweet = reader.read(line);
                longAdder.add(tweet == null ? 0 : 1);
            }
        }
    }

    private static void doJackson(List<String> lines, LongAdder longAdder, int split) {
        try {
            for (var i = 0; i < X / split; i++) {
                for (var line : lines) {
                    var tweet = objectMapper.readValue(line, Tweet.class);
                    longAdder.add(tweet == null ? 0 : 1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String perc(Duration par, Duration total) {
        return BigDecimal.valueOf(par.toMillis() * 100.0 / total.toMillis())
            .setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
