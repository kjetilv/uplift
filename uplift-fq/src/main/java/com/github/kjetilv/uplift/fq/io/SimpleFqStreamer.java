package com.github.kjetilv.uplift.fq.io;

import com.github.kjetilv.uplift.fq.FqPuller;
import com.github.kjetilv.uplift.fq.FqStreamer;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class SimpleFqStreamer<T>
    implements FqStreamer<T> {

    private final FqPuller<T> puller;

    public SimpleFqStreamer(FqPuller<T> puller) {
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
