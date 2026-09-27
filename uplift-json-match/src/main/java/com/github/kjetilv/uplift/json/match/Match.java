package com.github.kjetilv.uplift.json.match;

import module java.base;

public sealed interface Match<T> permits PathsMatch {

    default Stream<Probe<T>> leaves() {
        return pathways().stream().flatMap(Probe::leaves);
    }

    List<? extends Probe<T>> pathways();

    boolean matches();
}
