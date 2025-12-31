package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

public interface FlowRunner<T> {

    void run(Fqs<T> fqs, Flow<T> flow);
}
