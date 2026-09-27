package com.github.kjetilv.uplift.json.match;

import module java.base;

final class Print {

    static String trace(List<String> tr) {
        return tr == null
            ? "/"
            : tr.stream().flatMap(t -> Stream.of("/", t)).collect(Collectors.joining());
    }

    private Print() {
    }
}
