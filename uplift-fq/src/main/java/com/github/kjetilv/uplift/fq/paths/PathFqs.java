package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.*;

import java.nio.file.Path;
import java.util.Objects;

public final class PathFqs<I, O> implements Fqs<O> {

    public static <I, O> PathFqs<I, O> create(
        Path directory,
        Fio<I, O> fio,
        AccessProvider<Path, I> accessProvider,
        Dimensions dimensions
    ) {
        return new PathFqs<>(
            fio,
            new PathProvider(directory),
            accessProvider,
            dimensions
        );
    }

    private final Fio<I, O> fio;

    private final Dimensions dimensions;

    private final AccessProvider<Path, I> accessProvider;

    private final SourceProvider<Path> sourceProvider;

    public PathFqs(
        Fio<I, O> fio,
        SourceProvider<Path> sourceProvider,
        AccessProvider<Path, I> accessProvider,
        Dimensions dimensions
    ) {
        this.fio = Objects.requireNonNull(fio, "fio");
        this.sourceProvider = Objects.requireNonNull(sourceProvider, "sourceProvider");
        this.accessProvider = Objects.requireNonNull(accessProvider, "factories");
        this.dimensions = Objects.requireNonNull(dimensions, "dimensions");
    }

    @Override
    public FqPuller<O> puller(String name) {
        var directory = sourceProvider.source(name);
        return new PathFqPuller<>(
            directory,
            fio,
            accessProvider::puller,
            accessProvider.tombstone(directory),
            false
        );
    }

    @Override
    public FqWriter<O> writer(String name) {
        var directory = sourceProvider.source(name);
        return new PathFqWriter<>(
            directory,
            dimensions,
            accessProvider::writer,
            fio,
            accessProvider.tombstone(directory)
        );
    }
}
