package com.github.kjetilv.uplift.fq;

import java.util.stream.Stream;

public interface FqStreamer<T> extends FqReader<T> {

    static <T> FqStreamer<T> from(FqPuller<T> puller) {
        return new SimpleFqStreamer<>(puller);
    }

    Stream<T> read();
}
