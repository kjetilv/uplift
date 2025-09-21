package com.github.kjetilv.uplift.edam;

import module uplift.hash;

public record Handling<T, P extends Info<T, K>, K extends HashKind<K>>(
    Analysis<K> analysis,
    P payload
) {
}

