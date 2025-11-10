package com.github.kjetilv.uplift.json.match;

import java.util.Map;
import java.util.Optional;

public interface StructureDiffer<T> {

    Map<Pointer<T>, Diff<T>> subdiff(T subset);

    Optional<T> diff(T subset);
}
