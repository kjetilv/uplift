package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.SourceProvider;

import java.nio.file.Path;

public record PathProvider(Path root) implements SourceProvider<Path> {

    @Override
    public Path source(String name) {
        var path = Path.of(name);
        if (path.isAbsolute()) {
            throw new IllegalStateException("Expected non-absolute path, relative to " + root + ", got: " + name);
        }
        return root.resolve(path);
    }
}
