package com.github.kjetilv.uplift.json.match;

@SuppressWarnings("unused")
public final class Structures {

    public static <T> StructureMatcher<T> matcher(T obj, Structure<T> structure) {
        return matcher(obj, structure, null);
    }

    public static <T> StructureMatcher<T> matcher(T obj, Structure<T> structure, ArrayStrategy arrayStrategy) {
        return new DefaultStructureMatcher<>(obj, structure, arrayStrategy);
    }

    public static <T> StructureExtractor<T> extractor(T obj, Structure<T> structure) {
        return defaultImpl(obj, structure, null);
    }

    public static <T> StructureDiffer<T> differ(T obj, Structure<T> structure) {
        return defaultImpl(obj, structure, null);
    }

    public static <T> StructureExtractor<T> extractor(T obj, Structure<T> structure, ArrayStrategy arrayStrategy) {
        return defaultImpl(obj, structure, arrayStrategy);
    }

    public static final Structure<Object> MAPS = new MapsStructure();

    private Structures() {
    }

    private static <T> DefaultStructureMatcher<T> defaultImpl(
        T obj,
        Structure<T> structure,
        ArrayStrategy arrayStrategy
    ) {
        return new DefaultStructureMatcher<>(obj, structure, arrayStrategy);
    }

    public enum ArrayStrategy {

        SUBSET, SUBSEQ, EXACT
    }
}
