package com.github.kjetilv.uplift.json.match;

import module java.base;

public interface StructureExtractor<T> {

    Optional<T> extract(T mask);
}
