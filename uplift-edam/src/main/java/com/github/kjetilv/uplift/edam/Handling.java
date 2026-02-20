package com.github.kjetilv.uplift.edam;

import com.github.kjetilv.uplift.hash.HashKind;

public record Handling<T, P extends Info<T, K>, K extends HashKind<K>>(Analysis<K> analysis, P payload) {
}

