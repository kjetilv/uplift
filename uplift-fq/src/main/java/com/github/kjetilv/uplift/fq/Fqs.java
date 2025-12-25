package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.flows.Name;

import java.util.List;
import java.util.stream.Stream;

public interface Fqs<T> {

    default FqStreamer<T> streamer(Name name) {
        return FqStreamer.from(puller(name));
    }

    default FqBatcher<T> batcher(Name name, int batchSize) {
        return FqBatcher.from(puller(name), batchSize);
    }

    default Stream<T> stream(Name name) {
        return streamer(name).read();
    }

    default Stream<List<T>> batches(Name name, int batchSize) {
        return batcher(name, batchSize).read();
    }

    FqPuller<T> puller(Name name);

    FqWriter<T> writer(Name name);
}
