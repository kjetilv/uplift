import com.github.kjetilv.uplift.fq.flows.FqFlows;
import com.github.kjetilv.uplift.fq.flows.Name;
import com.github.kjetilv.uplift.fq.paths.AccessProviders;
import com.github.kjetilv.uplift.fq.paths.Dimensions;
import com.github.kjetilv.uplift.fq.paths.PathFqs;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.mame.CachingJsonSessions;
import com.github.kjetilv.uplift.util.SayFiles;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

void main() {
    var downloads = Path.of(System.getenv("HOME")).resolve("Downloads");
    var workDir = downloads.resolve("JEOPARDY");

    try {
        createDirectories(workDir);
    } catch (Exception e) {
        throw new IllegalStateException("Failed", e);
    }

    var jsonSessions = CachingJsonSessions.create(HashKind.K256);
    var json = Json.instance(jsonSessions);

    var fqs = PathFqs.create(
        workDir,
        json::jsonMap,
        value ->
            ByteBuffer.wrap(json.write(value).getBytes()),
        AccessProviders.channelBuffers(),
        new Dimensions(2, 4, 6)
    );

    var startFile = Path.of("JEOPARDY_QUESTIONS1.jsonl");

    setupStartFile(workDir, downloads, startFile, 0);

    var flows = FqFlows.builder(
            () -> startFile.getFileName().toString(),
            fqs
        )
        .fromSource().to(Stage.CAPITALIZE_ANSWER).with(items ->
            items.map(this::uppercaseAnswer))
        .then(Stage.REWRITE_CATEGORY).with(items ->
            items.map(this::rewriteCategory))
        .then(Stage.REWRITE_SHOW_NO).with(items ->
            items.map(this::countShowNo))
        .then(Stage.REWRITE_AIR_DATE).with(items ->
            items.map(this::airDate))
        .then(Stage.REWRITE_VALUE).with(items ->
            items.map(this::revalue))
        .build();

    if (flows.start()) {
        var run = flows.run();

        System.out.println(run.join().count());
    }
}

@SuppressWarnings("unchecked")
private <K, V> Map<K, V> uppercaseAnswer(Map<K, V> item) {
    var answer = item.get((K) "answer").toString();
    var kvMap = new HashMap<>(item);
    kvMap.put(
        (K) "answer",
        (V) upcase(answer)
    );
    return Map.copyOf(kvMap);
}

@SuppressWarnings("unchecked")
private <K, V> Map<K, V> rewriteCategory(Map<K, V> item) {
    var category = item.get((K) "category").toString();
    var kvMap = new HashMap<>(item);
    kvMap.put(
        (K) "category",
        (V) Arrays.stream(category.split("\\s+"))
            .map(this::downcase)
            .collect(Collectors.joining(" "))
    );
    return Map.copyOf(kvMap);
}

@SuppressWarnings("unchecked")
private <K, V> Map<K, V> countShowNo(Map<K, V> item) {
    var showNo = item.get((K) "show_number").toString();
    var kvMap = new HashMap<>(item);
    kvMap.remove((K) "show_number");
    kvMap.put(
        (K) "showNumber",
        (V) Integer.valueOf(Integer.parseInt(showNo))
    );
    return Map.copyOf(kvMap);
}

@SuppressWarnings("unchecked")
private <K, V> Map<K, V> airDate(Map<K, V> item) {
    var val = item.get((K) "air_date").toString();
    var kvMap = new HashMap<>(item);
    kvMap.remove((K) "air_date");
    kvMap.put(
        (K) "airDate",
        (V) val
    );
    return Map.copyOf(kvMap);
}

@SuppressWarnings("unchecked")
private <K, V> Map<K, V> revalue(Map<K, V> item) {
    var val = Optional.ofNullable(item.get((K) "value"))
        .map(Object::toString).orElse("0");
    var cleaned =
        val.replaceAll("\\$", "").replaceAll(",", "");
    var kvMap = new HashMap<>(item);
    kvMap.put(
        (K) "value",
        (V) Integer.valueOf(Integer.parseInt(cleaned))
    );
    return Map.copyOf(kvMap);
}

private String upcase(String str) {
    return str == null || str.isBlank() ? str
        : Character.toUpperCase(str.charAt(0)) + str.substring(1);
}

private String downcase(String str) {
    return str == null || str.isBlank() ? str
        : str.charAt(0) + str.substring(1).toLowerCase();
}

private static void setupStartFile(Path workDir, Path downloads, Path startFile, int size) {
    try {
        if (exists(workDir)) {
            try (var walk = walk(workDir).sorted(Comparator.reverseOrder())) {
                walk.forEach(SayFiles::delete);
            }
        }
        createDirectories(workDir);
        var source = downloads.resolve(startFile);
        var target = workDir.resolve(startFile);
        if (!exists(target) || exists(target) && size(target) != size(source)) {
            if (size <= 0) {
                copy(
                    source,
                    target,
                    COPY_ATTRIBUTES, REPLACE_EXISTING
                );
            } else {
                try (
                    var writer = SayFiles.newBufferedWriter(target);
                    var lines = lines(source)
                ) {
                    lines.limit(size)
                        .forEach(line -> {
                            try {
                                writer.write(line);
                                writer.write("\n");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                }

            }
        }
    } catch (Exception e) {
        throw new IllegalStateException("Failed", e);
    }
}

enum Stage implements Name {

    CAPITALIZE_ANSWER,
    REWRITE_CATEGORY,
    REWRITE_SHOW_NO,
    REWRITE_AIR_DATE,
    REWRITE_VALUE
}
