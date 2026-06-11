package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.JsonReader;
import com.github.kjetilv.uplift.json.JsonWriter;
import org.slf4j.Logger;

import javax.tools.Diagnostic;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;
import static org.slf4j.LoggerFactory.getLogger;

final class SessionsImpl {

    private static final Logger log = getLogger("javac");

    static Session session(
        String fqName,
        String source
    ) {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fm = compiler.getStandardFileManager(null, Locale.ROOT, UTF_8);

        var srcDir = temp("src");
        var classOut = temp("classes");
        var srcOut = temp("gen-src");

        var file = fqName.replace('.', '/') + ".java";
        var sourceFile = writeSource(srcDir, file, source);

        try {
            fm.setLocationFromPaths(CLASS_OUTPUT, List.of(classOut));
            fm.setLocationFromPaths(SOURCE_OUTPUT, List.of(srcOut));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set file manager locations on " + fm, e);
        }

        var url = url(classOut);
        var urlClassLoader = URLClassLoader.newInstance(
            new URL[] {url},
            Thread.currentThread().getContextClassLoader()
        );

        try (var compilerOut = new ByteArrayOutputStream()) {
            var units = fm.getJavaFileObjectsFromPaths(List.of(sourceFile));
            var task =
                compiler.getTask(
                    new PrintWriter(new OutputStreamWriter(compilerOut, UTF_8)),
                    fm,
                    diagnostic -> {
                        if (Objects.requireNonNull(diagnostic.getKind()) == Diagnostic.Kind.ERROR) {
                            throw new IllegalStateException("Compilation failure: " + diagnostic);
                        }
                        log.warn(diagnostic.toString());
                    },
                    null,
                    null,
                    units
                );
            task.setProcessors(List.of(new JsonRecordProcessor()));
            var ok = task.call();
            if (ok == null || !ok) {
                throw new IllegalStateException("Compilation failed for source file: " + sourceFile);
            }
            compilerOut.close();
            if (!compilerOut.toString(UTF_8).isEmpty()) {
                log.warn("Compilation produced output: {}", compilerOut.toString(UTF_8));
            }
            return new SessionImpl(
                fqName,
                source,
                srcOut,
                classOut,
                urlClassLoader,
                null
            );
        } catch (Exception e) {
            return new SessionImpl(
                fqName,
                source,
                srcOut,
                classOut,
                urlClassLoader,
                e);
        }
    }

    private SessionsImpl() {
    }

    private static URL url(Path classOut) {
        try {
            return classOut.toUri().toURL();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create URL for class output: " + classOut, e);
        }
    }

    private static Path writeSource(
        Path srcDir,
        String file,
        String source
    ) {
        var srcFile = srcDir.resolve(file);
        try {
            Files.createDirectories(srcFile.getParent());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write source file: " + file, e);
        }
        try {
            return Files.writeString(srcFile, source);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write source file: " + file, e);
        }
    }

    private static Path temp(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (Exception e) {
            throw new IllegalStateException("Could not create temp dir " + prefix, e);
        }
    }

    record SessionImpl(
        String fqName,
        String source,
        Path sourcegenDir,
        Path classesDir,
        ClassLoader classLoader,
        Exception compileError
    ) implements Session {

        @Override
        public Object read(String json) {
            JsonReader<String, ?> reader = rwMethod("stringReader");
            return type().cast(reader.read(json));
        }

        @Override
        public String write(Object object) {
            JsonWriter<String, Record, StringBuilder> writer = rwMethod("stringWriter");
            var stringBuilder = writer.write((Record) type().cast(object));
            return stringBuilder.toString();
        }

        @Override
        public List<Path> generatedFiles() {
            try (var list = Files.walk(sourcegenDir)) {
                return list
                    .filter(Files::isRegularFile)
                    .filter(path ->
                        path.getFileName().toString().endsWith(".java"))
                    .toList();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to list generated files in " + sourcegenDir, e);
            }
        }

        @Override
        public Path generatedFile(Path path) {
            return sourcegenDir.relativize(path);
        }

        private Class<?> type() {
            try {
                return classLoader.loadClass(fqName);
            } catch (Exception e) {
                throw new IllegalStateException("Could not find " + fqName, e);
            }
        }

        @SuppressWarnings("unchecked")
        private <T> T rwMethod(String methodName) {
            Object result;
            Class<?> rwClass;
            try {
                rwClass = classLoader.loadClass(fqName + "RW");
            } catch (Exception e1) {
                throw new IllegalStateException("Failed to load RW class", e1);
            }
            Field instanceField;
            try {
                instanceField = rwClass.getField("INSTANCE");
            } catch (Exception e1) {
                throw new IllegalStateException("Failed to get RW instance field", e1);
            }
            try {
                result = instanceField.get(null);
            } catch (Exception e1) {
                throw new IllegalStateException("Failed to get rw " + instanceField, e1);
            }
            var rw = result;
            Method method;
            try {
                method = JsonRW.class.getMethod(methodName);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to get method `" + methodName + "`", e);
            }
            try {
                return (T) method.invoke(rw);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to get method `" + methodName + "`", e);
            }
        }

    }
}
