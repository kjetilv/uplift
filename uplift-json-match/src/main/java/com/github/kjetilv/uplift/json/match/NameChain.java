package com.github.kjetilv.uplift.json.match;

import module java.base;

interface NameChain extends Supplier<Stream<String>> {

    default String path() {
        return get().collect(Collectors.joining("/"));
    }
}
