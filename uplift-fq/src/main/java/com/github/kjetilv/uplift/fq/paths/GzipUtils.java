package com.github.kjetilv.uplift.fq.paths;

import java.io.EOFException;
import java.nio.file.Path;

import static com.github.kjetilv.uplift.util.Throwables.chain;
import static java.nio.file.Files.size;

final class GzipUtils {

    static boolean incompleteHeader(Path path, Throwable e) {
        return chain(e).anyMatch(EOFException.class::isInstance) &&
               fileSize(path) <= GZIP_HEADER_SIZE;
    }

    private GzipUtils() {
    }

    private static final int GZIP_HEADER_SIZE = 80;

    private static long fileSize(Path path) {
        try {
            return size(path);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to assert size of " + path, e);
        }
    }
}
