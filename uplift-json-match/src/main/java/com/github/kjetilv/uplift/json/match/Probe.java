package com.github.kjetilv.uplift.json.match;

import java.util.List;
import java.util.stream.Stream;

public sealed interface Probe<T> permits Probe.Leaf, Probe.Node {

    default boolean found() {
        return successRate().is100Percent();
    }

    Rate successRate();

    List<String> trace();

    Stream<Probe<T>> leaves();

    Stream<String> lines(String indent, String delta);

    sealed interface Leaf<T> extends Probe<T>
        permits Probes.FoundLeaf, Probes.DeadLeaf {

        @Override
        default Stream<Probe<T>> leaves() {
            return Stream.of(this);
        }

        T main();
    }

    @SuppressWarnings("unused")
    sealed interface Node<T> extends Probe<T>
        permits Probes.FoundNode {

        List<Probe<T>> branches();
    }
}
