package com.github.kjetilv.uplift.fq;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

final class SimpleFqStreamer<T>
    implements FqStreamer<T> {

    private final FqPuller<T> puller;

    SimpleFqStreamer(FqPuller<T> puller) {
        this.puller = Objects.requireNonNull(puller, "puller");
    }

    @Override
    public Class<T> type() {
        return puller.type();
    }

    @Override
    public boolean done() {
        return puller.done();
    }

    @Override
    public String name() {
        return puller.name();
    }

    @Override
    public Stream<T> read() {
        return Stream.generate(puller::next)
            .takeWhile(Optional::isPresent)
            .map(Optional::get);
    }
}
