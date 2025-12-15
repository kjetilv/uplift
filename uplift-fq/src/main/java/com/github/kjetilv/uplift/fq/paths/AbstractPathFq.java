package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.Fq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.github.kjetilv.uplift.fq.paths.GzipUtils.gzipped;
import static com.github.kjetilv.uplift.fq.paths.GzipUtils.incompleteGZipHeader;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

abstract class AbstractPathFq<T> implements Fq<T> {

    private final Charset cs;

    private final Fio<T> fio;

    private final Path directory;

    private final Path tombstone;

    AbstractPathFq(Path directory, Fio<T> fio, Charset cs) {
        this.directory = Objects.requireNonNull(directory, "path");
        this.fio = Objects.requireNonNull(fio, "fio");
        this.tombstone = this.directory.resolve("done");
        this.cs = Objects.requireNonNull(cs, "cs");

        if (nonDirectory(this.directory)) {
            throw new IllegalStateException("Path must be a directory: " + directory);
        }

        if (nonWritable(directory)) {
            throw new IllegalStateException("Directory must be writable: " + directory);
        }
    }

    @Override
    public String name() {
        return directory.getFileName().toString();
    }

    @Override
    public Class<T> type() {
        return fio.type();
    }

    final Path tombstone() {
        return tombstone;
    }

    final Path directory() {
        return directory;
    }

    final String toString(T t) {
        return fio.write(t);
    }

    final T fromString(String line) {
        return fio.read(line);
    }

    final void failOnTombstone() {
        if (foundTombstone()) {
            throw new IllegalStateException("Tombstone was already set: " + tombstone());
        }
    }

    final boolean foundTombstone() {
        return exists(tombstone);
    }

    final List<Path> sortedFiles() {
        try (var list = list(directory())) {
            return list.sorted(BY_FILE_NAME)
                .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list files in " + directory(), e);
        }
    }

    final BufferedWriter tombstoneWriter() {
        try {
            return newBufferedWriter(tombstone, CREATE_NEW);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write tombstone", e);
        }
    }

    final BufferedReader bufferedReader(Path path) {
        Backoff backoff = null;
        var gzipped = gzipped(path);
        while (true) {
            try {
                var in = newInputStream(path);
                return new BufferedReader(
                    new InputStreamReader(
                        gzipped ? new GZIPInputStream(in) : in,
                        cs
                    ));
            } catch (Exception e) {
                if (gzipped && incompleteGZipHeader(path, e)) {
                    backoff = Backoff.sleepAndUpdate("Read " + path.getFileName(), backoff);
                } else {
                    throw new IllegalStateException("Could not read " + path, e);
                }
            }
        }
    }

    final BufferedWriter bufferedWriter(Path path) {
        try {
            var out = newOutputStream(path, CREATE_NEW);
            return new BufferedWriter(
                new OutputStreamWriter(
                    gzipped(path) ? new GZIPOutputStream(out) : out,
                    cs
                ));
        } catch (Exception e) {
            throw new IllegalStateException("Could not write to " + path, e);
        }
    }

    final void rm(Path path) {
        try {
            delete(path);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete " + path, e);
        }
    }

    private static final Comparator<Path> BY_FILE_NAME =
        Comparator.comparing(path -> path.getFileName().toString());

    private static boolean nonDirectory(Path directory) {
        return exists(directory) && !isDirectory(directory);
    }

    private static boolean nonWritable(Path directory) {
        return !(isWritable(directory) || couldCreate(directory));
    }

    private static boolean couldCreate(Path directory) {
        try {
            createDirectories(directory);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException("Could not create " + directory, e);
        }
    }
}
