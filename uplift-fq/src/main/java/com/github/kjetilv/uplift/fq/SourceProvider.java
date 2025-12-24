package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.data.Name;

public interface SourceProvider<S>{

    S source(Name name);
}
