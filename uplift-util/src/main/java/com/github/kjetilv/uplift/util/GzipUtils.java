package com.github.kjetilv.uplift.util;

import module java.base;

import static com.github.kjetilv.uplift.util.Throwables.chain;

public final class GzipUtils {

    public static boolean incompleteGZipHeader(Throwable e) {
        return chain(e).anyMatch(EOFException.class::isInstance);
    }

    public static boolean gzipped(Path path) {
        return path.getFileName().toString().endsWith(GZ);
    }

    public static Path to(Path source, Path target) {
        try (
            var is = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(source)));
            var os = new BufferedOutputStream(Files.newOutputStream(target))
        ) {
            is.transferTo(os);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return target;
    }

    public static Path gzipFile(Path path, boolean gzipped) {
        var fileName = path.getFileName().toString();
        if (gzipped) {
            if (fileName.endsWith(GZ)) {
                return path;
            }
            var zipped = Path.of(fileName + GZ);
            var parent = path.getParent();
            return parent == null
                ? zipped
                : parent.resolve(zipped);
        }
        if (fileName.endsWith(GZ)) {
            throw new IllegalStateException("Gzipped file name used for non-compressed data: " + path);
        }
        return path;
    }

    public static GZIPOutputStream zip(Object source, OutputStream out) {
        try {
            return new GZIPOutputStream(out, true);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write to " + source, e);
        }
    }

    public static GZIPInputStream unzip(Object source, InputStream in) {
        try {
            return new GZIPInputStream(in);
        } catch (Exception e) {
            throw new IllegalStateException("Could not read " + source, e);
        }
    }

    private GzipUtils() {
    }

    private static final String GZ = ".gz";
}
