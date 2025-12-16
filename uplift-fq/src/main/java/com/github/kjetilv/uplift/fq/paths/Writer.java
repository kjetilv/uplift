package com.github.kjetilv.uplift.fq.paths;

public interface Writer<I> {

    Writer<I> write(I line);

    void close();
}
