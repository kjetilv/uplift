package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;
import com.github.kjetilv.uplift.fq.data.Name;

import java.util.stream.Stream;

public interface FqFlows<T> {

    static <T> FqFlowsBuilder<T> builder(Name name, Fqs<T> fqs) {
        return new DefaultFqFlowsBuilder<>(name, fqs);
    }

    void feed(Stream<T> items);
}
