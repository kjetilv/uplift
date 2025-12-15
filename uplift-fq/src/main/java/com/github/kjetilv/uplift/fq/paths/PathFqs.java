package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqPuller;
import com.github.kjetilv.uplift.fq.FqWriter;
import com.github.kjetilv.uplift.fq.Fqs;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class PathFqs<T> implements Fqs<T> {

    private final Path root;

    private final Fio<T> fio;

    private final Dimensions dimensions;

    private final Charset cs;

    public PathFqs(Path root, Fio<T> fio, Dimensions dimensions) {
        this(root, fio, dimensions, null);
    }

    public PathFqs(Path root, Fio<T> fio, Dimensions dimensions, Charset cs) {
        this.root = Objects.requireNonNull(root, "root");
        this.fio = Objects.requireNonNull(fio, "fio");
        this.dimensions = Objects.requireNonNull(dimensions, "dimensions");
        this.cs = cs == null ? UTF_8 : cs;
    }

    @Override
    public FqPuller<T> puller(String name) {
        return new PathFqPuller<>(
            resolve(name),
            fio,
            false,
            cs
        );
    }

    @Override
    public FqWriter<T> writer(String name) {
        return new PathFqWriter<>(
            resolve(name),
            dimensions,
            fio,
            cs
        );
    }

    @Override
    public Stream<String> names() {
        return Stream.empty();
    }

    private Path resolve(String name) {
        var path = Path.of(name);
        if (path.isAbsolute()) {
            throw new IllegalStateException("Expected non-absolute path, relative to " + root + ", got: " + name);
        }
        return root.resolve(path);
    }
}
