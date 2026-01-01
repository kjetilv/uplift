package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.flows.Name;

public interface Fqs<T> {

    FqReader<T> reader(Name name);

    FqWriter<T> writer(Name name);

    void init(Name... names);
}
