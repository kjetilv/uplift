package com.github.kjetilv.uplift.jmh;

import module java.base;
import com.github.kjetilv.uplift.edamame.MapsMemoizers;
import org.openjdk.jmh.annotations.*;

import static com.github.kjetilv.uplift.hash.HashKind.K128;

/// Stores 20 000 parsed lines in a new memoizer per operation
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
public class MemoizerBenchmark {

    private List<Map<?, ?>> maps;

    @Setup
    public void setup() {
        maps = MemoizerData.maps(20_000);
    }

    @Benchmark
    public Object memoize() {
        var memoizer = MapsMemoizers.<Integer, K128>create(K128);
        for (var i = 0; i < maps.size(); i++) {
            memoizer.put(i, maps.get(i));
        }
        return memoizer.maps();
    }
}
