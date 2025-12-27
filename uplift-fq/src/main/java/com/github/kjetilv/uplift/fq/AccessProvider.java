package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.paths.Reader;
import com.github.kjetilv.uplift.fq.paths.Writer;

public interface AccessProvider<S, I> {

    Reader<I> reader(S source);

    Writer<I> writer(S source);
}
