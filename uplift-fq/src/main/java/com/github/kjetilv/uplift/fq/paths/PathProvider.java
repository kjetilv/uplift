package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.SourceProvider;
import com.github.kjetilv.uplift.fq.flows.Name;
import com.github.kjetilv.uplift.util.SayFiles;

import java.nio.file.Path;

public record PathProvider(Path root) implements SourceProvider<Path> {

    public PathProvider {
        if (SayFiles.nonDirectory(root)) {
            throw new IllegalArgumentException("Expected directory, got: " + root);
        }
    }

    @Override
    public Path source(Name name) {
        var path = Path.of(name.name());
        if (path.isAbsolute()) {
            throw new IllegalStateException("Expected non-absolute path, relative to " + root + ", got: " + name);
        }
        return root.resolve(path);
    }
}
