package com.github.kjetilv.uplift.asynchttp;

public interface Writable<B> {

    B buffer();

    int size();
}
