package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.*;
import com.github.kjetilv.uplift.fq.flows.Name;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

public final class PathFqs<I, O> implements Fqs<O> {

    public static <I, O> PathFqs<I, O> create(
        Path directory,
        Function<I, O> read,
        Function<O, I> write,
        AccessProvider<Path, I> accessProvider,
        Dimensions dimensions
    ) {
        return new PathFqs<>(
            new Fio<>() {

                @Override
                public O read(I line) {
                    return read.apply(line);
                }

                @Override
                public I write(O value) {
                    return write.apply(value);
                }
            },
            new PathProvider(directory),
            accessProvider,
            dimensions
        );
    }

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
    public FqReader<O> reader(Name name) {
        var path = sourceProvider.source(name);
        if (Files.isDirectory(path)) {
            return new PathFqReader<>(
                path,
                fio,
                accessProvider::reader,
                new PathTombstone(path.resolve("done")),
                false
            );
        }
        return new FileFqReader<>(fio, accessProvider.reader(path));
    }

    @Override
    public FqWriter<O> writer(Name name) {
        var directory = sourceProvider.source(name);
        return new PathFqWriter<>(
            directory,
            dimensions,
            accessProvider::writer,
            fio,
            new PathTombstone(directory.resolve("done"))
        );
    }
}
