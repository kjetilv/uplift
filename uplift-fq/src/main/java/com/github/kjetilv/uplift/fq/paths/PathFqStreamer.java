package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqPuller;
import com.github.kjetilv.uplift.fq.FqStreamer;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class PathFqStreamer<T>
    extends AbstractPathFqReader<T>
    implements FqStreamer<T> {

    private final FqPuller<T> puller;

    public PathFqStreamer(Path path, Fio<T> fio, FqPuller<T> puller, Charset cs) {
        super(path, fio, cs);
        this.puller = puller;
    }

    @Override
    public Stream<T> read() {
        return Stream.generate(puller::next)
            .takeWhile(Optional::isPresent)
            .map(Optional::get);
    }
}
