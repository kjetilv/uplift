package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.SourceProvider;
import com.github.kjetilv.uplift.fq.flows.Name;
import com.github.kjetilv.uplift.util.SayFiles;

import java.nio.file.Path;

public record PathProvider(Path root, String suffix) implements SourceProvider<Path> {

    public PathProvider {
        if (SayFiles.nonDirectory(root)) {
            throw new IllegalArgumentException("Expected directory, got: " + root);
        }
        if (suffix != null && suffix.startsWith(".")) {
            throw new IllegalArgumentException("Suffix should not have dot: " + suffix);
        }
    }

    public PathProvider(Path root) {
        this(root, null);
    }

    @Override
    public Path source(Name name) {
        var path = Path.of(name.name() + (suffix == null ? "" : suffix));
        if (path.isAbsolute()) {
            throw new IllegalStateException("Expected non-absolute path, relative to " + root + ", got: " + name);
        }
        return root.resolve(path);
    }
}
