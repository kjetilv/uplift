package com.github.kjetilv.uplift.json.gen;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface Session {

    static Session create(
        String fqName,
        String source
    ) {
        return SessionsImpl.session(fqName, source);
    }

    Throwable compileError();

    Object read(String json);

    String write(Object object);

    List<Path> generatedFiles();

    Path generatedFile(Path path);

    default boolean compilationFailed() {
        return compileError() != null;
    }

    default Object readAndVerify(String json) {
        var object = read(json);
        var json2 = write(object);
        var object2 = read(json2);
        assertThat(object2).isEqualTo(object);
        return object2;
    }
}
