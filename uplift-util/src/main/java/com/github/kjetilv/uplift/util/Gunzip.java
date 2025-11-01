package com.github.kjetilv.uplift.util;

import module java.base;

@SuppressWarnings("unused")
public final class Gunzip {

    public static Path toTemp(String resource) {
        var url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null) {
            throw new IllegalArgumentException("Could not find resource: " + resource);
        }
        Path source = sourcePath(resource, url);
        return to(source, tempVersion(source));
    }

    public static Path toTemp(Path source) {
        return to(source, tempVersion(source));
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

    private Gunzip() {
    }

    private static Path sourcePath(String resource, URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not resolve path for resource: " + resource, e);
        }
    }

    private static Path tempVersion(Path source) {
        var name = source.getFileName().toString();
        var dotIndex = name.lastIndexOf('.');
        try {
            return Files.createTempFile(name.substring(0, dotIndex), name.substring(dotIndex));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create temp file for: " + source, e);
        }
    }
}
