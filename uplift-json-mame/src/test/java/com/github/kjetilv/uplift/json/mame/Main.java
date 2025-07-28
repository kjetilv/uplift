package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Json;

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
        session.set(JsonSessions.create(K128));
        Json json = Json.INSTANCE;
        System.out.println("Before go: " + Mem.create());
        Instant goStart = Instant.now();
        Callbacks callbacks = session.get().onDone(list::add);
//        Callbacks callbacks = new ValueCallbacks(list::add);
        AtomicReference<Callbacks> cachingCallbacks = new AtomicReference<>(callbacks);
        lines(args).forEach(line ->
            json.parse(line, cachingCallbacks.get())
        );
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

    private static Stream<String> lines(String[] args) {
        return Arrays.stream(args)
            .parallel()
            .map(Path::of)
            .filter(Files::isRegularFile)
            .flatMap(path -> {
                try {
                    return Files.lines(path);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }
}
