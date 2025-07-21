package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.hash.HashKind;

import java.util.Map;

@FunctionalInterface
interface Canonicalizer<H extends HashKind<H>> {

    CanonicalValue<H> canonicalMap(Map<?, ?> value);
}
