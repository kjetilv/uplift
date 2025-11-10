package com.github.kjetilv.uplift.json.match;

@FunctionalInterface
public interface StructureMatcher<T> {

    Match<T> match(T part);

    default boolean contains(T part) {
        return match(part).matches();
    }
}
