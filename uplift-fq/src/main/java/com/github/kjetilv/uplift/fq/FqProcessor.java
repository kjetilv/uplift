package com.github.kjetilv.uplift.fq;

public interface FqProcessor<T, R> {

    R process(T t);
}
