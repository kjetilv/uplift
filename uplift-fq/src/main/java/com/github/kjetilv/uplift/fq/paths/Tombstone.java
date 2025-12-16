package com.github.kjetilv.uplift.fq.paths;

public interface Tombstone<T> {

    boolean isTombstone(T value);

    boolean isSet();

    void set(String inscription);
}
