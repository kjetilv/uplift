package com.github.kjetilv.uplift.json.match;

import module java.base;

sealed interface Path<T> permits Paths.Destination,
    Paths.ExactMatches,
    Paths.ExactObject,
    Paths.ObjectField,
    Paths.Subsequence,
    Paths.Subset {

    default Stream<Probe<T>> probe(T main) {
        return probe(main, null);
    }

    Stream<Probe<T>> probe(T main, List<String> trace);

    Optional<Extract<T>> extract(T main);
}
