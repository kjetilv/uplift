package com.github.kjetilv.uplift.json.match;

import java.util.List;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.json.match.Rate.FAILURE;
import static com.github.kjetilv.uplift.json.match.Rate.SUCCESS;

public sealed interface Probe<T> permits Probe.Leaf, Probe.Node {

    default boolean found() {
        return successRate().equals(SUCCESS);
    }

    Rate successRate();

    List<String> trace();

    Stream<Probe<T>> leaves();

    Stream<String> lines(String indent, String delta);

    record FoundNode<T>(List<Probe<T>> branches, List<String> trace) implements Node<T> {

        @Override
        public Rate successRate() {
            if (branches.isEmpty()) {
                return SUCCESS;
            }
            var count = branches.stream()
                .filter(Probe::found).count();
            return Rate.of(
                Math.toIntExact(count),
                branches.size()
            );
        }

        @Override
        public Stream<Probe<T>> leaves() {
            return branches.stream().flatMap(Probe::leaves);
        }

        @Override
        public Stream<String> lines(String indent, String delta) {
            return Stream.concat(
                Stream.of(getClass().getSimpleName() + "[[" + branches.size() + "]" + Print.trace(trace)),
                branches.stream().flatMap(sub ->
                        sub.lines(indent + delta, delta))
                    .map(line ->
                        delta + line)
            );
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + Print.trace(trace) + " -> " + branches + "]";
        }
    }

    record FoundLeaf<T>(T main, List<String> trace) implements Leaf<T> {

        @Override
        public Rate successRate() {
            return SUCCESS;
        }

        @Override
        public Stream<String> lines(String indent, String delta) {
            return Stream.of(
                getClass().getSimpleName() + "[" + Print.trace(trace) + "]:",
                delta + main
            );
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + Print.trace(trace) + ": " + main + "]";
        }
    }

    record DeadLeaf<T>(T main, T expected, List<String> trace) implements Leaf<T> {

        public static <T> DeadLeaf<T> deadEnd(T main, List<String> trace) {
            return new DeadLeaf<>(main, null, trace);
        }

        @Override
        public Rate successRate() {
            return FAILURE;
        }

        @Override
        public Stream<String> lines(String indent, String delta) {
            return Stream.of(
                getClass().getSimpleName() + "[" + Print.trace(trace) + "]",
                delta + new Diff<>(main, expected)
            );
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" +
                   Print.trace(trace) + " " + new Diff<>(main, expected) +
                   "]";
        }
    }

    sealed interface Leaf<T> extends Probe<T>
        permits FoundLeaf, DeadLeaf {

        @Override
        default Stream<Probe<T>> leaves() {
            return Stream.of(this);
        }

        T main();
    }

    @SuppressWarnings("unused")
    sealed interface Node<T> extends Probe<T>
        permits FoundNode {

        List<Probe<T>> branches();
    }
}
