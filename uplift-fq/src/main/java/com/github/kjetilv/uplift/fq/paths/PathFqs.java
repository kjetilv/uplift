package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.*;
import com.github.kjetilv.uplift.fq.flows.Name;
import com.github.kjetilv.uplift.util.SayFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static com.github.kjetilv.uplift.util.SayFiles.couldCreate;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isWritable;
import static java.util.Objects.requireNonNull;

public final class PathFqs<I, O> implements Fqs<O> {

    public static <I, O> PathFqs<I, O> create(
        Path directory,
        Function<I, O> read,
        Function<O, I> write,
        AccessProvider<Path, I> accessProvider,
        Dimensions dimensions
    ) {
        return create(
            directory,
            fio(
                read,
                write
            ),
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
            new PathProvider(directory, suffix(directory)),
            accessProvider,
            dimensions
        );
    }

    static Path ensureWritable(Path directory) {
        requireNonNull(directory, "directory");

        if (SayFiles.nonDirectory(directory)) {
            throw new IllegalArgumentException("Path was not a directory: " + directory);
        }

        if (exists(directory) && !isWritable(directory)) {
            throw new IllegalArgumentException("Directory was not writable: " + directory);
        }

        if (!exists(directory) && !couldCreate(directory)) {
            throw new IllegalArgumentException("Directory could not be created: " + directory);
        }

        return directory;
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
        this.fio = requireNonNull(fio, "fio");
        this.sourceProvider = requireNonNull(sourceProvider, "sourceProvider");
        this.accessProvider = requireNonNull(accessProvider, "factories");
        this.dimensions = requireNonNull(dimensions, "dimensions");
    }

    @Override
    public FqReader<O> reader(Name name) {
        var path = sourceProvider.source(name);
        if (Files.isRegularFile(path)) {
            return new FileFqReader<>(fio, accessProvider.reader(path));
        }
        return new PathFqReader<>(
            path,
            fio,
            accessProvider::reader,
            new PathTombstone(path.resolve("done")),
            false
        );
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

    @Override
    public void init(Name... names) {
        for (Name name : names) {
            ensureWritable(sourceProvider.source(name));
        }
    }

    private static String suffix(Path directory) {
        var fileName = directory.getFileName().toString();
        var index = fileName.lastIndexOf('.');
        var suffix = index == -1 ? null : fileName.substring(index + 1);
        return suffix;
    }

    private static <I, O> Fio<I, O> fio(
        Function<I, O> read,
        Function<O, I> write
    ) {
        return new Fio<>() {

            @Override
            public O read(I line) {
                return read.apply(line);
            }

            @Override
            public I write(O value) {
                return write.apply(value);
            }
        };
    }
}
