package com.github.kjetilv.uplift.jmh;

import module java.base;
import com.github.kjetilv.uplift.json.JsonReader;
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

/// Reads JSON into [Tweet] records, with generated uplift readers and with Jackson databind.
///
/// * `*Bytes`, `*String`, `*Stream`: One pretty-printed tweet, `48.json`, from each input type
/// * `*Lines`: All compact tweets in `48.jsonl`, one `byte[]` per tweet
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class TweetReadBenchmark {

    private ObjectMapper objectMapper;

    private JsonReader<byte[], Tweet> bytesReader;

    private JsonReader<String, Tweet> stringReader;

    private JsonReader<InputStream, Tweet> streamReader;

    private byte[] bytes;

    private String string;

    private List<byte[]> lines;

    @Setup
    public void setup() {
        this.objectMapper = BenchmarkData.objectMapper();
        this.bytesReader = TweetRW.INSTANCE.bytesReader();
        this.stringReader = TweetRW.INSTANCE.stringReader();
        this.streamReader = TweetRW.INSTANCE.streamReader();
        this.bytes = BenchmarkData.bytes(BenchmarkData.TWEET);
        this.string = new String(bytes, StandardCharsets.UTF_8);
        this.lines = BenchmarkData.lines(BenchmarkData.TWEET_LINES, Integer.MAX_VALUE);
        verify();
    }

    @Benchmark
    public Tweet upliftBytes() {
        return bytesReader.read(bytes);
    }

    @Benchmark
    public Tweet jacksonBytes() {
        return objectMapper.readValue(bytes, Tweet.class);
    }

    @Benchmark
    public Tweet upliftString() {
        return stringReader.read(string);
    }

    @Benchmark
    public Tweet jacksonString() {
        return objectMapper.readValue(string, Tweet.class);
    }

    @Benchmark
    public Tweet upliftStream() {
        return streamReader.read(new ByteArrayInputStream(bytes));
    }

    @Benchmark
    public Tweet jacksonStream() {
        return objectMapper.readValue(new ByteArrayInputStream(bytes), Tweet.class);
    }

    @Benchmark
    public void upliftLines(Blackhole blackhole) {
        for (var line : lines) {
            blackhole.consume(bytesReader.read(line));
        }
    }

    @Benchmark
    public void jacksonLines(Blackhole blackhole) {
        for (var line : lines) {
            blackhole.consume(objectMapper.readValue(line, Tweet.class));
        }
    }

    /// Fails the trial if either library cannot read the input
    private void verify() {
        BenchmarkData.nonNull(
            Stream.of(upliftBytes(), jacksonBytes(), upliftString(), jacksonString(), upliftStream(), jacksonStream())
                .toList(),
            "Single tweet reads"
        );
        BenchmarkData.nonNull(lines.stream().map(bytesReader::read).toList(), "uplift line reads");
        BenchmarkData.nonNull(
            lines.stream().map(line -> objectMapper.readValue(line, Tweet.class)).toList(),
            "Jackson line reads"
        );
    }
}
