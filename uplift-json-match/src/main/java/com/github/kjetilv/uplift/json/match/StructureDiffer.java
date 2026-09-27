package com.github.kjetilv.uplift.json.match;

import module java.base;

public interface StructureDiffer<T> {

    Map<Pointer<T>, Diff<T>> subdiff(T subset);

    Optional<T> diff(T subset);
}
