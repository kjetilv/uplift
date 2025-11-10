package com.github.kjetilv.uplift.json.match;

import java.util.List;
import java.util.stream.Stream;

final class Probes {

    private Probes() {

    }

    record FoundNode<T>(List<Probe<T>> branches, List<String> trace) implements Probe.Node<T> {

        @Override
        public Rate successRate() {
            if (branches.isEmpty()) {
                return Rate.SUCCESS;
            }
            var count = branches.stream()
                .filter(Probe::found).count();
            return Rate.of(
                Math.toIntExact(count),
                branches.size()
            );
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

        @Override
        public Stream<Probe<T>> leaves() {
            return branches.stream().flatMap(Probe::leaves);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + Print.trace(trace) + " -> " + branches + "]";
        }
    }

    record FoundLeaf<T>(T main, List<String> trace) implements Probe.Leaf<T> {

        @Override
        public Rate successRate() {
            return Rate.SUCCESS;
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

    record DeadLeaf<T>(T main, T expected, List<String> trace) implements Probe.Leaf<T> {

        public static <T> DeadLeaf<T> deadEnd(T main, List<String> trace) {
            return new DeadLeaf<>(main, null, trace);
        }

        @Override
        public Rate successRate() {
            return Rate.FAILURE;
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
}
