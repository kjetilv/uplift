package com.github.kjetilv.uplift.fq.paths;

public interface Reader<I> {

    I read();

    void close();
}
