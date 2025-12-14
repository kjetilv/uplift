package com.github.kjetilv.uplift.fq;

public interface Fq<T> {

    String name();

    Class<T> type();

    boolean done();
}
