package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqPuller;
import com.github.kjetilv.uplift.fq.FqWriter;
import com.github.kjetilv.uplift.fq.Fqs;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.github.kjetilv.uplift.fq.paths.GzipUtils.gzipped;
import static com.github.kjetilv.uplift.fq.paths.GzipUtils.incompleteGZipHeader;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public final class PathFqs<T> implements Fqs<T> {

    private final Path root;

    private final Fio<byte[], T> fio;

    private final Dimensions dimensions;

    public PathFqs(Path root, Fio<byte[], T> fio, Dimensions dimensions) {
        this.root = Objects.requireNonNull(root, "root");
        this.fio = Objects.requireNonNull(fio, "fio");
        this.dimensions = Objects.requireNonNull(dimensions, "dimensions");
    }

    @Override
    public FqPuller<T> puller(String name) {
        var directory = resolve(name);
        return new PathFqPuller<>(
            directory,
            fio,
            this::puller,
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
            this::writer,
            fio,
            new PathTombstone(directory.resolve("done"))
        );
    }

    private Puller<byte[]> puller(Path path) {
        return new StreamPuller(path, inputStream(path));
    }

    private InputStream inputStream(Path path) {
        Backoff backoff = null;
        var gzipped = gzipped(path);
        while (true) {
            try {
                var in = newInputStream(path);
                return gzipped
                    ? new GZIPInputStream(in)
                    : in;
            } catch (Exception e) {
                if (gzipped && incompleteGZipHeader(path, e)) {
                    backoff = Backoff.sleepAndUpdate("Read " + path.getFileName(), backoff);
                } else {
                    throw new IllegalStateException("Could not read " + path, e);
                }
            }
        }
    }

    private Writer<byte[]> writer(Path path) {
        return new StreamWriter(bufferedWriter(path));
    }

    private OutputStream bufferedWriter(Path path) {
        try {
            var out = newOutputStream(path, CREATE_NEW);
            return gzipped(path) ? new GZIPOutputStream(out, true) : out;
        } catch (Exception e) {
            throw new IllegalStateException("Could not write to " + path, e);
        }
    }

    private Path resolve(String name) {
        var path = Path.of(name);
        if (path.isAbsolute()) {
            throw new IllegalStateException("Expected non-absolute path, relative to " + root + ", got: " + name);
        }
        return root.resolve(path);
    }
}
