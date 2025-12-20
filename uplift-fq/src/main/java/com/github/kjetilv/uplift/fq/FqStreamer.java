package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.io.SimpleFqStreamer;

import java.util.stream.Stream;

public interface FqStreamer<T> extends FqReader<T> {

    static <T> FqStreamer<T> from(FqPuller<T> puller) {
        return new SimpleFqStreamer<>(puller);
    }

    Stream<T> read();
}
