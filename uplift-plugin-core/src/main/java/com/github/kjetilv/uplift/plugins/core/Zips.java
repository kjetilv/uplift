package com.github.kjetilv.uplift.plugins.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Zips {

    /**
     * AWS Lambda expects the custom runtime entry point to be named `bootstrap`, and the
     * stack builders declare `handler("bootstrap")`. The single entry name is therefore
     * part of the contract and must not change.
     */
    public static final String ENTRY = "bootstrap";

    public static Path zip(Path source, Path zipFile) {
        Path target = zipFile != null ? zipFile : Path.of(source + ".zip");
        try (
            OutputStream out = Files.newOutputStream(target);
            ZipOutputStream zip = new ZipOutputStream(out)
        ) {
            zip.putNextEntry(new ZipEntry(ENTRY));
            try (InputStream in = Files.newInputStream(source)) {
                in.transferTo(zip);
            }
            zip.closeEntry();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to zip " + source + " to " + target, e);
        }
        return target;
    }

    private Zips() {
    }
}
