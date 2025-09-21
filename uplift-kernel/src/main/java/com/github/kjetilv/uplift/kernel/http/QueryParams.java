package com.github.kjetilv.uplift.kernel.http;

import module java.base;

import static com.github.kjetilv.uplift.util.CaseInsensitiveHashMap.caseInsensitive;

@SuppressWarnings("ALL")
public final class QueryParams {

    public static Map<String, List<String>> read(String query) {
        Map<String, List<String[]>> entries = Arrays.stream(query.split("&"))
            .map(pair -> pair.split("="))
            .collect(Collectors.groupingBy(
                (String[] pair) -> pair[0],
                Collectors.toList()
            ));
        return entries.entrySet().stream()
            .collect(caseInsensitive(
                Map.Entry::getKey,
                entry ->
                    entry.getValue()
                        .stream()
                        .map(pair -> pair[1])
                        .toList()
            ));
    }

    public static Look get(String path) {
        Map<String, List<String>> map = read(path);
        return key ->
            Optional.ofNullable(map.get(key))
                .map(List::stream)
                .flatMap(Stream::findFirst);
    }

    private QueryParams() {
    }

    @FunctionalInterface
    public interface Look {

        Optional<String> up(String key);
    }
}
