package com.github.kjetilv.uplift.fq;

import java.util.stream.Stream;

public interface FqStreamer<T> extends Fq<T> {

    static <T> FqStreamer<T> from(FqPuller<T> puller) {
        return new FqStreamerImpl<>(puller);
    }

    Stream<T> read();
}
