package com.github.kjetilv.uplift.json.gen;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public interface Session {

    static Session create(
        String source
    ) {
        return SessionsImpl.session(source);
    }

    Throwable compileError();

    Object read(String json);

    String write(Object object);

    Path generatedDir();

    Optional<Path> packageDir();

    default Path generatedFilesDir() {
        return generatedFiles().stream().map(Path::getParent).distinct().findFirst().orElseThrow(() ->
            new IllegalStateException(this + ": No generated files"));
    }

    List<Path> generatedFiles();

    Path generatedFile(Path path);

    default boolean compilationFailed() {
        return compileError() != null;
    }

    default Object readAndVerify(String json) {
        var object = read(json);
        var rewrittenJson = write(object);
        assertThat(rewrittenJson)
            .isNotNull()
            .isNotBlank();
        var rereadObject = read(rewrittenJson);
        assertThat(rereadObject)
            .isNotNull()
            .describedAs(
                "Re-read object different from original: %s <> %s (original)", rereadObject, object)
            .isEqualTo(object);
        return rereadObject;
    }
}
