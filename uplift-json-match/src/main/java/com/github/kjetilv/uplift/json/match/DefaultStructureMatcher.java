package com.github.kjetilv.uplift.json.match;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

record DefaultStructureMatcher<T>(T main, Structure<T> str, Structures.ArrayStrategy arr)
    implements StructureMatcher<T>, StructureExtractor<T>, StructureDiffer<T> {

    DefaultStructureMatcher {
        Objects.requireNonNull(main, "main");
        Objects.requireNonNull(str, "str");
    }

    @Override
    public Match<T> match(T part) {
        return new PathsMatch<>(pathsIn(part)
            .flatMap(path -> path.probe(main))
            .toList());
    }

    @Override
    public Optional<T> extract(T mask) {
        return pathsIn(mask)
            .map(path -> path.extract(main))
            .flatMap(Optional::stream)
            .map(Extract::value)
            .reduce(str::combine);
    }

    @Override
    public Map<Pointer<T>, Diff<T>> subdiff(T subset) {
        return Maps.toMap(pointersIn(subset)
            .sorted(Comparator.naturalOrder())
            .map(pointer ->
                Map.entry(
                    pointer, pointer.get(subset)
                        .filter(value -> !str.isNull(value))
                        .map(value ->
                            new Diff<>(value, pointer.get(main).orElse(null)))
                ))
            .filter(entry ->
                entry.getValue()
                    .filter(Diff::isDiff).isPresent())
            .map(entry ->
                Map.entry(entry.getKey(), entry.getValue().get()))
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<T> diff(T subset) {
        return (Optional<T>) subdiff(subset).entrySet()
            .stream()
            .map(entry -> entry.getKey()
                .map(entry.getValue().found()))
            .reduce(Combine::objects)
            .map(Map.class::cast)
            .map(diff ->
                str.toObject(diff));
    }

    private Stream<Pointer<T>> pointersIn(T part) {
        if (str.isObject(part)) {
            if (str.namedFields(part).findAny().isEmpty()) {
                return empty();
            }
            return str.mapNamedFields(
                part, (name, subpart) ->
                    pointersIn(subpart).map(pointer ->
                        new Pointer.Node<>(name, pointer, str))
            );
        }
        if (str.isArray(part)) {
            if (str.isEmpty(part)) {
                return empty();
            }
            var index = new AtomicInteger();
            Supplier<Integer> nextIndex = index::getAndIncrement;
            return str.mapArrayElements(
                part, element ->
                    pointersIn(element).map(pointer ->
                        new Pointer.Array<>(nextIndex.get(), pointer, str))
            );
        }
        return empty();
    }

    private Stream<Path<T>> pathsIn(T part) {
        if (str.isObject(part)) {
            return Stream.of(new Paths.ExactObject<>(
                str.mapNamedFields(
                        part, (name, subpart) ->
                            pathsIn(subpart).map(path ->
                                new Paths.ObjectField<>(name, path, str))
                    )
                    .toList(),
                str
            ));
        }
        if (str.isArray(part)) {
            return switch (arr) {
                case EXACT -> exact(arrayPaths(part));
                case SUBSET -> subset(arrayPaths(part));
                case null, default -> subseq(arrayPaths(part));
            };
        }
        return Stream.of(new Paths.Destination<>(part));
    }

    private Stream<Path<T>> arrayPaths(T part) {
        return str.mapArrayElements(part, this::pathsIn);
    }

    private Stream<Path<T>> exact(Stream<Path<T>> paths) {
        return Stream.of(new Paths.ExactMatches<>(paths.toList(), str));
    }

    private Stream<Path<T>> subseq(Stream<Path<T>> paths) {
        return Stream.of(new Paths.Subsequence<>(paths.toList(), str));
    }

    private Stream<Path<T>> subset(Stream<Path<T>> paths) {
        return paths.map(path -> new Paths.Subset<>(path, str));
    }

    private static final Pointer.Leaf<?> EMPTY_LEAF = new Pointer.Leaf<>();

    private static <T> Stream<Pointer<T>> empty() {
        return Stream.of(emptyLeaf());
    }

    @SuppressWarnings("unchecked")
    private static <T> Pointer.Leaf<T> emptyLeaf() {
        return (Pointer.Leaf<T>) EMPTY_LEAF;
    }

}
