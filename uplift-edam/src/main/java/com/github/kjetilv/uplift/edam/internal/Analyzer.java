package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.Analysis;
import com.github.kjetilv.uplift.hash.HashKind;

public interface Analyzer<T, H extends HashKind<H>> {

    Analysis<H> analyze(T item);
}
