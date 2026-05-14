package com.github.kjetilv.uplift.edam;

import com.github.kjetilv.uplift.hash.HashKind;

public record Handling<T, P extends Info<T, H>, H extends HashKind<H>>(Analysis<H> analysis, P payload) {
}
