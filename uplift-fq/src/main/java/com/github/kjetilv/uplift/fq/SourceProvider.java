package com.github.kjetilv.uplift.fq;

public interface SourceProvider<S>{

    S source(String name);
}
