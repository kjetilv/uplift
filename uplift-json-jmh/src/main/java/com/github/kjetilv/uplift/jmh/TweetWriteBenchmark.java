package com.github.kjetilv.uplift.jmh;

import module java.base;
import com.github.kjetilv.uplift.json.JsonWriter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import tools.jackson.databind.ObjectMapper;

/// Writes [Tweet] records as JSON, with generated uplift writers and with Jackson databind.
///
/// The records are read once, with uplift, during setup. Both libraries write the same instances.
///
/// * `*String`, `*Bytes`: The tweet in `48.json`
/// * `*Lines`: All tweets in `48.jsonl`, one `byte[]` per tweet
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class TweetWriteBenchmark {

    private ObjectMapper objectMapper;

    private JsonWriter<String, Tweet, StringBuilder> stringWriter;

    private JsonWriter<byte[], Tweet, ByteArrayOutputStream> bytesWriter;

    private Tweet tweet;

    private List<Tweet> tweets;

    @Setup
    public void setup() {
        this.objectMapper = BenchmarkData.objectMapper();
        this.stringWriter = TweetRW.INSTANCE.stringWriter();
        this.bytesWriter = TweetRW.INSTANCE.bytesWriter();
        var reader = TweetRW.INSTANCE.bytesReader();
        this.tweet = reader.read(BenchmarkData.bytes(BenchmarkData.TWEET));
        this.tweets = BenchmarkData.nonNull(
            BenchmarkData.lines(BenchmarkData.TWEET_LINES, Integer.MAX_VALUE).stream()
                .map(reader::read)
                .toList(),
            "uplift line reads"
        );
        BenchmarkData.nonNull(Stream.of(tweet).toList(), "uplift read");
    }

    @Benchmark
    public String upliftString() {
        return stringWriter.write(tweet).toString();
    }

    @Benchmark
    public String jacksonString() {
        return objectMapper.writeValueAsString(tweet);
    }

    @Benchmark
    public byte[] upliftBytes() {
        return bytesWriter.write(tweet).toByteArray();
    }

    @Benchmark
    public byte[] jacksonBytes() {
        return objectMapper.writeValueAsBytes(tweet);
    }

    @Benchmark
    public void upliftLines(Blackhole blackhole) {
        for (var line : tweets) {
            blackhole.consume(bytesWriter.write(line).toByteArray());
        }
    }

    @Benchmark
    public void jacksonLines(Blackhole blackhole) {
        for (var line : tweets) {
            blackhole.consume(objectMapper.writeValueAsBytes(line));
        }
    }
}
