package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.JsonSession;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.hash.HashKind.K128;

public class Main {

    static void main(String[] args) {
        List<Object> list = Collections.synchronizedList(new ArrayList<>());
        List<Object> unhashed = Collections.synchronizedList(new ArrayList<>());

        AtomicReference<JsonSession> session = new AtomicReference<>();
        session.set(CachingJsonSessions.create(K128));
        System.out.println("Before go: " + Mem.create());
        Instant goStart = Instant.now();
        Callbacks callbacks = session.get().callbacks(list::add);
//        Callbacks callbacks = new ValueCallbacks(list::add);
        AtomicReference<Callbacks> cachingCallbacks = new AtomicReference<>(callbacks);
        Json json = Json.instance(CachingJsonSessions.create(K128));
        if (Arrays.stream(args).anyMatch(arg -> arg.endsWith(".jsonl"))) {
            lines(args).forEach(line ->
                json.parse(line, cachingCallbacks.get())
            );
        } else {
            Arrays.stream(args)
                .forEach(arg -> {
                    try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(Path.of(arg)))) {
                        json.parse(inputStream, cachingCallbacks.get());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }
        long putTime = Duration.between(goStart, Instant.now()).toMillis();
        System.out.println("After put (" + putTime + "ms): " + Mem.create());
        System.gc();
        System.out.println(list.size());
        System.out.println(unhashed.size());
        System.out.println("After parse: " + Mem.create());
        Instant gcStart = Instant.now();
        cachingCallbacks.set(null);
        session.set(null);
        System.gc();
        long gcTime = Duration.between(gcStart, Instant.now()).toMillis();
        System.out.println("After GC (" + gcTime + "ms): " + Mem.create());
    }

    private Main() {
    }

    private static Stream<String> lines(String[] args) {
        List<Path> paths = Arrays.stream(args)
            .parallel()
            .map(Path::of)
            .filter(Files::isRegularFile)
            .toList();
        return paths.stream().flatMap(path -> {
            try {
                return Files.lines(path);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
