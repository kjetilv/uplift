package com.github.kjetilv.uplift.jmh;

import module java.base;
import com.github.kjetilv.uplift.json.Json;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import tools.jackson.databind.ObjectMapper;

/// Reads and writes untyped JSON, as maps and lists, with uplift [Json] and with Jackson databind.
///
/// The input is the first [#lines] lines of the bundled `iso_deliverables_metadata.jsonl.gz`, held in memory
/// as one `byte[]` per line. Write benchmarks write the same map instances, read once with uplift during setup.
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class MapBenchmark {

    @Param("1000")
    public int lines;

    private Json json;

    private ObjectMapper objectMapper;

    private List<byte[]> data;

    private List<Object> maps;

    @Setup
    public void setup() {
        this.json = Json.instance();
        this.objectMapper = BenchmarkData.mapObjectMapper();
        this.data = BenchmarkData.lines(BenchmarkData.ISO_LINES, lines);
        this.maps = BenchmarkData.nonNull(data.stream().map(json::read).toList(), "uplift reads");
        BenchmarkData.nonNull(
            data.stream().map(line -> objectMapper.readValue(line, Map.class)).toList(),
            "Jackson reads"
        );
    }

    @Benchmark
    public void upliftRead(Blackhole blackhole) {
        for (var line : data) {
            blackhole.consume(json.read(line));
        }
    }

    @Benchmark
    public void jacksonRead(Blackhole blackhole) {
        for (var line : data) {
            blackhole.consume(objectMapper.readValue(line, Map.class));
        }
    }

    @Benchmark
    public void jacksonReadTree(Blackhole blackhole) {
        for (var line : data) {
            blackhole.consume(objectMapper.readTree(line));
        }
    }

    @Benchmark
    public void upliftWrite(Blackhole blackhole) {
        for (var map : maps) {
            blackhole.consume(json.writeBytes(map));
        }
    }

    @Benchmark
    public void jacksonWrite(Blackhole blackhole) {
        for (var map : maps) {
            blackhole.consume(objectMapper.writeValueAsBytes(map));
        }
    }
}
