package com.github.kjetilv.uplift.json.match;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Print {

    static String trace(List<String> tr) {
        return tr == null
            ? "/"
            : tr.stream().flatMap(t -> Stream.of("/", t)).collect(Collectors.joining());
    }

    private Print() {
    }
}
