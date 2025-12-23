package com.github.kjetilv.uplift.fq.paths;

public interface Tombstone<S> {

    boolean isTombstone(S value);

    boolean isSet();

    void set(String inscription);
}
