package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.hash.HashKind;

@FunctionalInterface
interface Canonicalizer<H extends HashKind<H>> {

    CanonicalValue<H> canonical(Object value);
}
