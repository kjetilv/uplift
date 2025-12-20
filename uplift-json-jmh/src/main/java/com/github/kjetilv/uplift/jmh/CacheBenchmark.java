package com.github.kjetilv.uplift.jmh;

import module java.base;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.mame.CachingJsonSessions;
import com.github.kjetilv.uplift.util.GzipUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

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
        var jsonSession = CachingJsonSessions.create(K128);
        var json = Json.instance(jsonSession);
        try (var lines = Files.lines(tmp)) {
            List<Object> list = new ArrayList<>();
            var callbacks = jsonSession.callbacks(list::add);
            lines.forEach(line ->
                json.parse(line, callbacks)
            );
            return list;
        }
    }

    @Benchmark
    public Object readJsonUncached() throws Exception {
        var json = Json.instance();
        try (var lines = Files.lines(tmp)) {
            return lines.map(json::read)
                .toList();
        }
    }

    @Benchmark
    public Object readJsonJackson() throws Exception {
        var objectMapper = new ObjectMapper();
        try (var lines = Files.lines(tmp)) {
            return lines.map(line -> {
                    try {
                        return objectMapper.readValue(line, Map.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        }
    }

    private static final Path tmp = toTemp("iso_deliverables_metadata.jsonl.gz");

    private static Path toTemp(String resource) {
        var url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null) {
            throw new IllegalArgumentException("Could not find resource: " + resource);
        }
        var source = sourcePath(resource, url);
        return GzipUtils.to(source, tempVersion(source));
    }

    private static Path sourcePath(String resource, URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not resolve path for resource: " + resource, e);
        }
    }

    private static Path tempVersion(Path source) {
        var name = source.getFileName().toString();
        var dotIndex = name.lastIndexOf('.');
        try {
            return Files.createTempFile(name.substring(0, dotIndex), name.substring(dotIndex));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create temp file for: " + source, e);
        }
    }
}

