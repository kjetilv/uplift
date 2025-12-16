package com.github.kjetilv.uplift.fq.paths;

import java.io.EOFException;
import java.nio.file.Path;

import static com.github.kjetilv.uplift.util.Throwables.chain;

final class GzipUtils {

    static boolean incompleteGZipHeader(Path path, Throwable e) {
        return chain(e).anyMatch(EOFException.class::isInstance);
    }

    static boolean gzipped(Path path) {
        return path.getFileName().toString().endsWith(".gz");
    }

    private GzipUtils() {
    }
}
