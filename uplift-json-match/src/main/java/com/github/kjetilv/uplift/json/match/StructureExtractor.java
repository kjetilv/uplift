package com.github.kjetilv.uplift.json.match;

import java.util.Optional;

public interface StructureExtractor<T> {

    Optional<T> extract(T mask);
}
