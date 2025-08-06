package com.github.kjetilv.uplift.kernel.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

public final class Gunzip {

    public static Path toTemp(String resource) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null) {
            throw new IllegalArgumentException("Could not find resource: " + resource);
        }
        Path source;
        try {
            URI uri = url.toURI();
            source = Paths.get(uri);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not resolve path for resource: " + resource, e);
        }
        return to(source, tempVersion(source));
    }

    public static Path toTemp(Path source) {
        return to(source, tempVersion(source));
    }

    public static Path to(Path source, Path target) {
        try (
            InputStream is = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(source)));
            BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(target))
        ) {
            is.transferTo(os);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return target;
    }

    private Gunzip() {

    }

    private static Path tempVersion(Path source) {
        String name = source.getFileName().toString();
        int dotIndex = name.lastIndexOf('.');
        try {
            return Files.createTempFile(name.substring(0, dotIndex), name.substring(dotIndex));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create temp file for: " + source, e);
        }
    }
}
