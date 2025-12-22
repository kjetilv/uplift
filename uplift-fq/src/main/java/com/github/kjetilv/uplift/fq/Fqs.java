package com.github.kjetilv.uplift.fq;

import java.util.List;
import java.util.stream.Stream;

public interface Fqs<T> {

    default FqStreamer<T> streamer(String name) {
        return FqStreamer.from(puller(name));
    }

    default FqBatcher<T> batcher(String name, int batchSize) {
        return FqBatcher.from(puller(name), batchSize);
    }

    default Stream<T> stream(String name) {
        return streamer(name).read();
    }

    default Stream<List<T>> batches(String name, int batchSize) {
        return batcher(name, batchSize).read();
    }

    FqPuller<T> puller(String name);

    FqWriter<T> writer(String name);
}
