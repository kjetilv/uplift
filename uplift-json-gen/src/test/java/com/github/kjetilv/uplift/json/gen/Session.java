package com.github.kjetilv.uplift.json.gen;

import module java.base;

import static org.assertj.core.api.Assertions.assertThat;

public interface Session {

    static Session create(String source) {
        return create(source, (Path) null);
    }

    static Session create(String source, Path tempDirectory) {
        return SessionsImpl.session(source, tempDirectory);
    }

    default Path generatedFilesDir() {
        return generatedFiles().stream()
            .map(Path::getParent).distinct()
            .findFirst().orElseThrow(() ->
                new IllegalStateException(this + ": No generated files"));
    }

    default boolean compilationFailed() {
        return compileError() != null;
    }

    default void readAndVerify(String json) {
        var object = read(json);
        var rewrittenJson = write(object);
        assertThat(rewrittenJson)
            .describedAs("No json written for " + object)
            .isNotNull()
            .describedAs("Blank json written for " + object)
            .isNotBlank();
        var rereadObject = read(rewrittenJson);
        assertThat(rereadObject)
            .describedAs("Null parsed from: " + rewrittenJson)
            .isNotNull()
            .describedAs("Re-read object different from original: %s <> %s (original)", rereadObject, object)
            .isEqualTo(object);
    }

    Throwable compileError();

    Object read(String json);

    String write(Object object);

    Path generatedDir();

    Optional<Path> packageDir();

    List<Path> generatedFiles();

    Path generatedFile(Path path);
}
