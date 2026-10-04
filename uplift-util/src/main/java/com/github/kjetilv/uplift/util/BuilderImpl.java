package com.github.kjetilv.uplift.util;

import module java.base;

final class BuilderImpl implements ScriptRun.Builder {

    private final Path path;

    private final List<ScriptRun.LineListener> listeners = new ArrayList<>();

    private Path cwd;

    private String[] args;

    BuilderImpl(Path path) {
        this.path = path;
    }

    @Override
    public ScriptRun.Builder command(String... args) {
        this.args = args.clone();
        return this;
    }

    @Override
    public ScriptRun.Builder cwd(Path path) {
        if (cwd != null && !Files.isDirectory(cwd)) {
            throw new IllegalArgumentException("Not a directory: " + path);
        }
        this.cwd = path;
        return this;
    }

    @Override
    public ScriptRun.Builder listen(ScriptRun.LineListener... listeners) {
        Arrays.stream(listeners)
            .filter(Objects::nonNull)
            .forEach(this.listeners::add);
        return this;
    }

    @Override
    public ScriptRun run() {
        return new ScriptRun(path, processBuilder(), listeners, args);
    }

    private ProcessBuilder processBuilder() {
        try {
            var processBuilder = new ProcessBuilder(commands());
            if (cwd != null) {
                processBuilder.directory(cwd.toFile());
            }
            return processBuilder;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create procss builder", e);
        }
    }

    private List<String> commands() {
        return Stream.concat(
                Stream.of(path)
                    .map(Path::toAbsolutePath)
                    .map(Path::toString),
                Arrays.stream(args)
            )
            .toList();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + path + " @" + cwd + "]";
    }
}
