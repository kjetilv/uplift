package com.github.kjetilv.uplift.json.match;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class Paths {

    private Paths() {
    }

    private static <T> Stream<Probe<T>> exactArrayPaths(
        List<String> trace,
        List<T> mainElements,
        List<Path<T>> paths
    ) {
        var pathsSize = paths.size();
        var mainSize = mainElements.size();
        if (pathsSize < mainSize) {
            return Stream.concat(
                matches(mainElements, paths, trace),
                mainElements.subList(pathsSize, mainSize)
                    .stream()
                    .map(element ->
                        Probes.DeadLeaf.deadEnd(element, trace))
            );
        }
        if (pathsSize > mainSize) {
            return Stream.concat(
                matches(mainElements, paths, trace),
                paths.subList(mainSize, pathsSize)
                    .stream()
                    .flatMap(path ->
                        path.probe(null, trace))
            );
        }
        return matches(mainElements, paths, trace);
    }

    private static <T> Stream<Probe<T>> matches(List<T> mainElements, List<Path<T>> paths, List<String> trace) {
        return Zip.of(paths, mainElements)
            .map(Zip.Idxd::t)
            .flatMap(pathNode ->
                pathNode.p1().probe(pathNode.p2(), trace));
    }

    record ExactMatches<T>(List<Path<T>> paths, Structure<T> structure)
        implements Path<T> {

        @Override
        public Stream<Probe<T>> probe(T main, List<String> trace) {
            return exactArrayPaths(
                trace,
                structure.listElements(main),
                paths
            );
        }

        @Override
        public Optional<Extract<T>> extract(T main) {
            return Optional.ofNullable(main)
                .map(value -> () -> value);
        }
    }

    record Subset<T>(Path<T> path, Structure<T> structure)
        implements Path<T> {

        @Override
        public Stream<Probe<T>> probe(T main, List<String> trace) {
            return Stream.of(structure.arrayElements(main)
                .flatMap(node ->
                    path.probe(node, trace))
                .filter(Probe::found)
                .findFirst()
                .orElseGet(() -> new Probes.DeadLeaf<>(main, null, trace)));
        }

        @Override
        public Optional<Extract<T>> extract(T main) {
            return Optional.ofNullable(main)
                .map(value -> () -> value);
        }
    }

    record ExactObject<T>(List<ObjectField<T>> objectFields, Structure<T> structure)
        implements Path<T> {

        @Override
        public Stream<Probe<T>> probe(T main, List<String> trace) {
            return Stream.of(new Probes.FoundNode<>(
                branches(main, trace),
                trace
            ));
        }

        @Override
        public Optional<Extract<T>> extract(T main) {
            return Optional.of(
                    Maps.toMap(objectFields.stream()
                        .map(objectField ->
                            entry(main, objectField)
                        ).flatMap(Optional::stream)
                    ))
                .filter(map ->
                    !map.isEmpty())
                .map(map -> () ->
                    structure.toObject(map));
        }

        private Optional<Map.Entry<String, T>> entry(T main, ObjectField<T> objectField) {
            return objectField.extract(main)
                .map(Extract::value)
                .map(value ->
                    Map.entry(objectField.name(), value));
        }

        private List<Probe<T>> branches(T main, List<String> trace) {
            return objectFields.stream()
                .flatMap(objectField ->
                    objectField.probe(main, trace))
                .toList();
        }
    }

    record Subsequence<T>(List<Path<T>> paths, Structure<T> structure)
        implements Path<T> {

        @Override
        public Stream<Probe<T>> probe(T main, List<String> trace) {
            var mainElements = structure.listElements(main);
            if (mainElements.size() < paths.size()) {
                return deadEnd(main, trace);
            }
            return match(trace, mainElements).map(startIndex ->
                    exactArrayPaths(
                        trace,
                        mainElements.subList(
                            startIndex,
                            Math.min(mainElements.size(), startIndex + paths.size())
                        ),
                        paths
                    ))
                .orElseGet(() ->
                    deadEnd(main, trace));

        }

        @Override
        public Optional<Extract<T>> extract(T main) {
            return Optional.ofNullable(main)
                .map(value -> () -> value);
        }

        private Optional<Integer> match(List<String> trace, List<T> mainElements) {
            return IntStream.range(0, mainElements.size())
                .filter(index ->
                    paths.getFirst()
                        .probe(mainElements.get(index), trace)
                        .allMatch(Probe::found))
                .boxed()
                .findFirst();
        }

        private static <T> Stream<Probe<T>> deadEnd(
            T main,
            List<String> trace
        ) {
            return Stream.of(Probes.DeadLeaf.deadEnd(main, trace));
        }
    }

    record ObjectField<T>(String name, Path<T> next, Structure<T> structure)
        implements Path<T> {

        @Override
        public Stream<Probe<T>> probe(T main, List<String> trace) {
            return structure.get(main, name)
                .map(field ->
                    next.probe(field, addTo(trace, name)))
                .orElseGet(() ->
                    Stream.of(Probes.DeadLeaf.deadEnd(main, trace)));
        }

        @Override
        public Optional<Extract<T>> extract(T main) {
            return structure.get(main, name).flatMap(next::extract);
        }

        private static List<String> addTo(List<String> list, String added) {
            return list == null
                ? Collections.singletonList(added)
                : Stream.concat(list.stream(), Stream.of(added))
                    .toList();
        }
    }

    record Destination<T>(T expected)
        implements Path<T> {

        @Override
        public Stream<Probe<T>> probe(T main, List<String> trace) {
            return Optional.ofNullable(main)
                .filter(expected::equals)
                .map(_ ->
                    found(expected, trace))
                .or(() ->
                    unexpected(main, expected, trace))
                .stream();
        }

        @Override
        public Optional<Extract<T>> extract(T main) {
            return Optional.ofNullable(main)
                .map(value -> () -> value);
        }

        private static <T> Probe<T> found(T expected, List<String> trace) {
            return new Probes.FoundLeaf<>(expected, trace);
        }

        private static <T> Optional<Probe<T>> unexpected(
            T main,
            T expected,
            List<String> trace
        ) {
            return Optional.of(new Probes.DeadLeaf<>(main, expected, trace));
        }
    }
}
