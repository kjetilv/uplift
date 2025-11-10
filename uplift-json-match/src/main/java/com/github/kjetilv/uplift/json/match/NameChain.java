package com.github.kjetilv.uplift.json.match;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

interface NameChain extends Supplier<Stream<String>> {

    default String path() {
        return get().collect(Collectors.joining("/"));
    }
}
