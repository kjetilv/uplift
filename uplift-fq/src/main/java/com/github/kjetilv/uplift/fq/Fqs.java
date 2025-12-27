package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.flows.Name;

import java.util.List;
import java.util.stream.Stream;

public interface Fqs<T> {

    default Stream<T> stream(Name name) {
        return reader(name).stream();
    }

    default Stream<List<T>> batches(Name name, int batchSize) {
        return reader(name).batches(batchSize);
    }

    FqReader<T> reader(Name name);

    FqWriter<T> writer(Name name);
}
