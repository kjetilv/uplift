package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Json;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class Main {

    static void main(String[] args) {
        List<Object> list = new ArrayList<>();
        List<Object> unhashed = new ArrayList<>();
        AtomicReference<JsonSession>
            session = new AtomicReference<>(JsonSessions.create(
            HashKind.K128,
            object -> unhashed.add(object)
        ));
        Json json = Json.INSTANCE;
        System.out.println("Before go: " + Mem.create());

        AtomicReference<Callbacks> cachingCallbacks =
            new AtomicReference<>(session.get().onDone(list::add));
        lines(args).forEach(line ->
            json.parse(line, cachingCallbacks.get())
        );

        System.gc();
        System.out.println(list.size());
        System.out.println(unhashed.size());
        System.out.println("After parse:" + Mem.create());
        cachingCallbacks.set(null);
        session.set(null);
        System.gc();
        System.out.println("After GC: " + Mem.create());
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
