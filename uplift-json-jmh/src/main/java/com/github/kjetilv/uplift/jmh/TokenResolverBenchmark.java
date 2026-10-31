package com.github.kjetilv.uplift.jmh;

import module java.base;
import com.github.kjetilv.uplift.json.TokenResolver;
import com.github.kjetilv.uplift.json.gen.tokens.TokenTable;
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

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/// Resolves every field name in `48.json`, in document order, against the field names of [Tweet].
/// Names that are not fields of [Tweet] are included, as they occur in real data.
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class TokenResolverBenchmark {

    private TokenResolver tokenResolver;

    private byte[] buffer;

    private int[] offsets;

    private int[] lengths;

    @Setup
    public void setup() {
        this.tokenResolver = new TokenTable(Tweet_Callbacks.PRESETS.fields().toList());
        try (var in = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("48.json"))) {
            this.buffer = in.readAllBytes();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read 48.json", e);
        }
        // ISO-8859-1 maps bytes to chars one to one, so string indexes are byte offsets
        var matches = FIELD_NAME.matcher(new String(buffer, ISO_8859_1))
            .results()
            .toList();
        this.offsets = matches.stream().mapToInt(match -> match.start(1)).toArray();
        this.lengths = matches.stream().mapToInt(match -> match.end(1) - match.start(1)).toArray();
    }

    @Benchmark
    public void resolveFieldNames(Blackhole blackhole) {
        for (var i = 0; i < offsets.length; i++) {
            blackhole.consume(tokenResolver.get(buffer, offsets[i], lengths[i]));
        }
    }

    private static final Pattern FIELD_NAME = Pattern.compile("\"([^\"\\\\]*)\"\\s*:");
}
