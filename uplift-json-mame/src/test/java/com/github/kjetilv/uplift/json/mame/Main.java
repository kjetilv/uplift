package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Json;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    static void main(String[] args) {
        List<Object> list = new ArrayList<>();
        List<Object> unhashed = new ArrayList<>();
        JsonSession session = JsonSessions.create(HashKind.K128, object -> unhashed.add(object));
        Json json = Json.INSTANCE;
        lines(args).forEach(
            line ->
                json.parse(line, session.onDone(list::add)));
        System.out.println(list.size());
        System.out.println(unhashed.size());
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
