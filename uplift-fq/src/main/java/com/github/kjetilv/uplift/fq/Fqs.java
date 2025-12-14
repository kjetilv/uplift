package com.github.kjetilv.uplift.fq;

import java.util.stream.Stream;

public interface Fqs<T> {

    FqStreamer<T> stream(String name);

    FqPuller<T> pull(String name);

    FqWriter<T> write(String name);

    Stream<String> names();
}
