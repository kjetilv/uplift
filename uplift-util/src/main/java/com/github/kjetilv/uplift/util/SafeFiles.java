package com.github.kjetilv.uplift.util;

import module java.base;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

@SuppressWarnings("unused")
public final class SafeFiles {

    public static OutputStream newFileOutputStream(String path) {
        return newFileOutputStream(Paths.get(path));
    }

    public static OutputStream newFileOutputStream(Path path) {
        try {
            return Files.newOutputStream(path, CREATE_NEW);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write to " + path, e);
        }
    }

    public static BufferedWriter newBufferedWriter(Path path) {
        try {
            return Files.newBufferedWriter(path, CREATE_NEW);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write to " + path, e);
        }
    }

    public static InputStream fileInputStream(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (Exception e) {
            throw new IllegalStateException("Could not read from " + path, e);
        }
    }

    public static Stream<Path> list(Path directory) {
        try {
            return Files.list(directory);
        } catch (Exception e) {
            throw new IllegalStateException("Could not list files in " + directory, e);
        }
    }

    public static long sizeOf(Path path) {
        if (isDirectory(path)) {
            throw new IllegalStateException("Not a file: " + path);
        }
        if (exists(path)) {
            try {
                return Files.size(path);
            } catch (Exception e) {
                throw new IllegalStateException("Could not find size of " + path, e);
            }
        }
        return 0L;
    }

    public static boolean emptyDirectory(Path directory) {
        return isDirectory(directory) && list(directory).findAny().isEmpty() ||
               couldCreate(directory);
    }

    public static boolean nonDirectory(Path directory) {
        return exists(directory) && !isDirectory(directory);
    }

    public static void delete(Path path) {
        try {
            Files.delete(path);
        } catch (Exception e) {
            throw new IllegalStateException("Could not delete " + path, e);
        }
    }

    public static boolean couldCreate(Path directory) {
        try {
            Files.createDirectories(directory);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException("Could not create " + directory, e);
        }
    }

    private SafeFiles() {
    }
}
