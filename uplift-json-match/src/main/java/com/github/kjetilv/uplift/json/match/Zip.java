package com.github.kjetilv.uplift.json.match;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class Zip {

    static <X, Y> Stream<Idxd<Pair<X, Y>>> of(List<X> p1s, List<Y> p2s) {
        return IntStream.range(0, Math.min(p1s.size(), p2s.size()))
            .mapToObj(index ->
                new Idxd<>(index, new Pair<>(p1s.get(index), p2s.get(index))));
    }

    private Zip() {
    }

    record Pair<P1, P2>(P1 p1, P2 p2) {}

    record Idxd<T>(int index, T t) {}
}
