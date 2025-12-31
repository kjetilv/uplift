package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.util.SayFiles;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.file.Files.delete;

abstract class AbstractPathFq<I, T> {

    private final Fio<I, T> fio;

    private final Path directory;

    private final Tombstone<Path> tombstone;

    AbstractPathFq(Path directory, Fio<I, T> fio, Tombstone<Path> tombstone) {
        this.directory = Objects.requireNonNull(directory, "path");
        this.fio = Objects.requireNonNull(fio, "fio");
        this.tombstone = Objects.requireNonNull(tombstone, "tombstone");

        if (SayFiles.nonDirectory(this.directory)) {
            throw new IllegalStateException("Path must be a directory: " + directory);
        }

        if (SayFiles.nonWritable(directory)) {
            throw new IllegalStateException("Directory must be writable: " + directory);
        }
    }

    final Path directory() {
        return directory;
    }

    final I toOutput(T t) {
        return fio.write(t);
    }

    final T fromInput(I line) {
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

    final Stream<Path> sortedFiles() {
        try {
            var list = SayFiles.list(directory());
            return list.sorted(BY_FILE_NAME);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list files in " + directory(), e);
        }
    }

    final void rm(Path path) {
        try {
            delete(path);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete " + path, e);
        }
    }

    final String name() {
        return directory.getFileName().toString();
    }

    protected abstract void subToString(StringBuilder builder);

    private static final Comparator<Path> BY_FILE_NAME =
        Comparator.comparing(path -> path.getFileName().toString());

    @Override
    public final String toString() {
        var sb = new StringBuilder(getClass().getSimpleName())
            .append("[").append(directory).append(": ");
        subToString(sb);
        return sb.append("]").toString();
    }
}
