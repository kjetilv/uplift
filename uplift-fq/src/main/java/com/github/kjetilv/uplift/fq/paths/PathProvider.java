package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.SourceProvider;
import com.github.kjetilv.uplift.fq.data.Name;

import java.nio.file.Path;

public record PathProvider(Path root) implements SourceProvider<Path> {

    @Override
    public Path source(Name name) {
        var path = Path.of(name.name());
        if (path.isAbsolute()) {
            throw new IllegalStateException("Expected non-absolute path, relative to " + root + ", got: " + name);
        }
        return root.resolve(path);
    }
}
