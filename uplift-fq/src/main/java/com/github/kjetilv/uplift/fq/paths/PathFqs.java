package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqPuller;
import com.github.kjetilv.uplift.fq.FqWriter;
import com.github.kjetilv.uplift.fq.Fqs;
import com.github.kjetilv.uplift.fq.paths.bytes.StreamFactories;

import java.nio.file.Path;
import java.util.Objects;

public final class PathFqs<T> implements Fqs<T> {

    private final Path root;

    private final Fio<byte[], T> fio;

    private final Dimensions dimensions;

    private final StreamFactories factories;

    public PathFqs(
        Path root,
        Fio<byte[], T> fio,
        Dimensions dimensions
    ) {
        this.root = Objects.requireNonNull(root, "root");
        this.fio = Objects.requireNonNull(fio, "fio");
        this.dimensions = Objects.requireNonNull(dimensions, "dimensions");
        this.factories = new StreamFactories();
    }

    @Override
    public FqPuller<T> puller(String name) {
        var directory = resolve(name);
        return new PathFqPuller<>(
            directory,
            fio,
            factories::puller,
            new PathTombstone(directory.resolve("done")),
            false
        );
    }

    @Override
    public FqWriter<T> writer(String name) {
        var directory = resolve(name);
        return new PathFqWriter<>(
            directory,
            dimensions,
            factories::writer,
            fio,
            new PathTombstone(directory.resolve("done"))
        );
    }

    private Path resolve(String name) {
        var path = Path.of(name);
        if (path.isAbsolute()) {
            throw new IllegalStateException("Expected non-absolute path, relative to " + root + ", got: " + name);
        }
        return root.resolve(path);
    }
}
