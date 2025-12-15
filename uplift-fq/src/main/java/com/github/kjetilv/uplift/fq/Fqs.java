package com.github.kjetilv.uplift.fq;

import java.util.stream.Stream;

public interface Fqs<T> {

    default FqStreamer<T> streamer(String name) {
        return FqStreamer.from(puller(name));
    }

    FqPuller<T> puller(String name);

    default FqBatcher<T> batcher(String name, int batchSize) {
        return FqBatcher.from(puller(name), batchSize);
    }

    FqWriter<T> writer(String name);

    Stream<String> names();
}
