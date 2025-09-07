package com.github.kjetilv.uplift.jmh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.JsonSession;
import com.github.kjetilv.uplift.json.mame.CachingJsonSessions;
import com.github.kjetilv.uplift.util.Gunzip;
import org.openjdk.jmh.annotations.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.hash.HashKind.K128;

@Fork
@Warmup(iterations = 1, time = 10)
@Measurement(iterations = 1, time = 10)
public class CacheBenchmark {
//    static void main() throws Exception {
//        new CacheBenchmark().readJsonCached();
//    }

    @Benchmark
    public Object readJsonCached() throws Exception {
        JsonSession jsonSession = CachingJsonSessions.create(K128);
        Json json = Json.instance(jsonSession);
        try (Stream<String> lines = Files.lines(tmp)) {
            List<Object> list = new ArrayList<>();
            Callbacks callbacks = jsonSession.callbacks(list::add);
            lines.forEach(line ->
                json.parse(line, callbacks)
            );
            return list;
        }
    }

    @Benchmark
    public Object readJsonUncached() throws Exception {
        Json json = Json.instance();
        try (Stream<String> lines = Files.lines(tmp)) {
            return lines.map(json::read).toList();
        }
    }


    @Benchmark
    public Object readJsonJackson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try (Stream<String> lines = Files.lines(tmp)) {
            return lines.map(line -> {
                try {
                    return objectMapper.readValue(line, Map.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).toList();
        }
    }

    private static final Path tmp = Gunzip.toTemp("iso_deliverables_metadata.jsonl.gz");
}

