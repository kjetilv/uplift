package com.github.kjetilv.uplift.plugins.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class FileIO {

    public static boolean isZip(Path path) {
        return path.getFileName().toString().endsWith(".zip");
    }

    public static boolean isJar(Path path) {
        return path.getFileName().toString().endsWith(".jar");
    }

    public static boolean nonDirectory(Path path) {
        return !Files.isDirectory(path);
    }

    public static void copyTo(Path source, Path context) {
        copyTo(source, context, null);
    }

    /**
     * Copies only when the target is absent, a different size, or older. Returns the
     * target either way.
     */
    public static void copyTo(Path source, Path context, String target) {
        Path targetPath = context.resolve(target != null ? Path.of(target) : source.getFileName());
        try {
            if (shouldCopy(source, targetPath)) {
                Files.copy(source, targetPath, REPLACE_EXISTING, COPY_ATTRIBUTES);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to copy " + source + " to " + targetPath, e);
        }
    }

    public static void clearRecursive(Path... paths) {
        clearRecursive(List.of(paths));
    }

    public static void clearRecursive(List<Path> paths) {
        paths.forEach(FileIO::clear);
    }

    private static void clear(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (Stream<Path> list = Files.list(path)) {
                    list.forEach(FileIO::clear);
                }
            }
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to clear " + path, e);
        }
    }

    private static boolean shouldCopy(Path source, Path target) throws IOException {
        return !Files.exists(target)
               || Files.size(source) != Files.size(target)
               || modified(source).isAfter(modified(target));
    }

    private static Instant modified(Path path) throws IOException {
        return Files.getLastModifiedTime(path).toInstant();
    }

    private FileIO() {
    }
}
