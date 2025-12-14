package com.github.kjetilv.uplift.fq;

import java.util.stream.Stream;

public interface FqStreamer<T> extends FqReader<T> {

    Stream<T> read();
}
