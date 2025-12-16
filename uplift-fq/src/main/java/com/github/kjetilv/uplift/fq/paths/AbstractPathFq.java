package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.Fq;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import static com.github.kjetilv.uplift.fq.paths.GzipUtils.gzipped;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

abstract class AbstractPathFq<T> implements Fq<T> {

    private final Fio<byte[], T> fio;

    private final Path directory;

    private final Tombstone<Path> tombstone;

    AbstractPathFq(Path directory, Fio<byte[], T> fio, Tombstone<Path> tombstone) {
        this.directory = Objects.requireNonNull(directory, "path");
        this.fio = Objects.requireNonNull(fio, "fio");
        this.tombstone = Objects.requireNonNull(tombstone, "tombstone");

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

    final Path directory() {
        return directory;
    }

    final byte[] toBytes(T t) {
        return fio.write(t);
    }

    final T fromBytes(byte[] line) {
        return fio.read(line);
    }

    final void failOnTombstone() {
        if (tombstone.isSet()) {
            throw new IllegalStateException("Tombstone was already set: " + tombstone);
        }
    }

    final boolean foundTombstone() {
        return tombstone.isSet();
    }

    final boolean isTombstone(Path file) {
        return tombstone.isTombstone(file);
    }

    final void setTombstone(String inscription) {
        if (tombstone.isSet()) {
            throw new IllegalStateException("Tombstone already set: " + tombstone);
        }
        tombstone.set(inscription);
    }

    final List<Path> sortedFiles() {
        try (var list = list(directory())) {
            return list.sorted(BY_FILE_NAME)
                .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list files in " + directory(), e);
        }
    }

    final OutputStream bufferedWriter(Path path) {
        try {
            var out = newOutputStream(path, CREATE_NEW);
            return gzipped(path) ? new GZIPOutputStream(out, true) : out;
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
