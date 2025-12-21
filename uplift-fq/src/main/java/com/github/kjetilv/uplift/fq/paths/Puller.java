package com.github.kjetilv.uplift.fq.paths;

public interface Puller<I> {

    I pull();

    void close();
}
