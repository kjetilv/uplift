import module java.base;
import com.github.kjetilv.uplift.fq.flows.FqFlows;
import com.github.kjetilv.uplift.fq.flows.Name;
import com.github.kjetilv.uplift.fq.paths.AccessProviders;
import com.github.kjetilv.uplift.fq.paths.Dimensions;
import com.github.kjetilv.uplift.fq.paths.PathFqs;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.mame.CachingJsonSessions;
import com.github.kjetilv.uplift.util.SayFiles;

import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

void main() {
    var downloads = Path.of(System.getenv("HOME")).resolve("Downloads");
    var workDir = downloads.resolve("JEOPARDY.jsonl");

    try {
        createDirectories(workDir);
    } catch (Exception e) {
        throw new IllegalStateException("Failed", e);
    }

    var jsonSessions = CachingJsonSessions.create(HashKind.K256);
    var json = Json.instance(jsonSessions);

    var dimensions = new Dimensions(2, 4, 6);

    var fqs =
        PathFqs.create(
            workDir,
            json::map,
            value ->
                ByteBuffer.wrap(json.write(value).getBytes()),
            AccessProviders.channelBuffers(),
            dimensions
        );
//        PathFqs.create(
//        workDir,
//        new IdentityFio<>(),
//        new JsonAccessProvider(
//            json,
//            Arena::ofAuto,
//            (byte) '\n'
//        ),
//        dimensions
//    );

    var startFile = Path.of("JEOPARDY_QUESTIONS1.jsonl");

    setupStartFile(workDir, downloads, startFile, 0);

    var flows = FqFlows.builder(startFile.getFileName(), fqs)
        .timeout(Duration.ofSeconds(30))
        .batchSize(10)
        .init(
            Stage.ANSWER,
            process(this::uppercaseAnswer)
        )
        .then(
            Stage.CATEGORY,
            process(this::rewriteCategory)
        )
        .then(
            Stage.SHOW_NO,
            process(this::countShowNo)
        )
        .then(
            Stage.AIR_DATE,
            process(this::airDate)
        )
        .then(
            Stage.VALUE,
            process(this::revalue)
        )
        .build();

    Instant now = Instant.now();
    if (flows.start()) {
        var run = flows.run();
        run.join()
            .forEach(System.out::println);
    } else {
        throw new IllegalStateException("Not started:" + flows);
    }
    var timeTaken = Duration.between(now, Instant.now());
    System.out.println("Time taken: " + timeTaken);
}

private FqFlows.Processor<Map<String, Object>> process(
    Function<Map<String, Object>, Map<String, Object>> process
) {
    return items -> items.map(process);
}

private static Map<String, Object> roundtrip(Map<String, Object> map) {
    return Json.INSTANCE.map(Json.INSTANCE.write(map));
}

private Map<String, Object> uppercaseAnswer(Map<String, Object> item) {
    var answer = item.get("answer").toString();
    var kvMap = new HashMap<>(item);
    kvMap.put(
        "answer",
        upcase(answer)
    );
    return kvMap;
}

private Map<String, Object> rewriteCategory(Map<String, Object> item) {
    var category = item.get("category").toString();
    var kvMap = new HashMap<>(item);
    kvMap.put(
        "category",
        Arrays.stream(WS.split(category))
            .map(this::downcase)
            .collect(Collectors.joining(" "))
    );
    return kvMap;
}

private Map<String, Object> countShowNo(Map<String, Object> item) {
    var showNo = item.get("show_number").toString();
    var kvMap = new HashMap<>(item);
    kvMap.remove("show_number");
    kvMap.put(
        "showNumber",
        Integer.parseInt(showNo)
    );
    return kvMap;
}

private Map<String, Object> airDate(Map<String, Object> item) {
    var val = item.get("air_date").toString();
    var kvMap = new HashMap<>(item);
    kvMap.remove("air_date");
    kvMap.put(
        "airDate",
        val
    );
    return kvMap;
}

private Map<String, Object> revalue(Map<String, Object> item) {
    var val = Optional.ofNullable(item.get("value"))
        .map(Object::toString).orElse("0");
    var kvMap = new HashMap<>(item);
    kvMap.put("value", computeValue(val));
    return kvMap;
}

private String upcase(String str) {
    return str == null || str.isBlank() ? str
        : Character.toUpperCase(str.charAt(0)) + str.substring(1);
}

private String downcase(String str) {
    return str == null || str.isBlank() ? str
        : str.charAt(0) + str.substring(1).toLowerCase();
}

private static final Pattern WS = Pattern.compile("\\s+");

private static int computeValue(String val) {
    var cleaned =
        val.replaceAll("\\$", "").replaceAll(",", "");
    return Integer.parseInt(cleaned);
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

    ANSWER,
    CATEGORY,
    SHOW_NO,
    AIR_DATE,
    VALUE
}
