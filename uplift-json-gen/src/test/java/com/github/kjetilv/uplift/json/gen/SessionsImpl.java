package com.github.kjetilv.uplift.json.gen;

import module java.base;
import com.github.kjetilv.uplift.json.JsonReader;
import com.github.kjetilv.uplift.json.JsonWriter;
import org.slf4j.Logger;

import javax.tools.Diagnostic;
import javax.tools.ToolProvider;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;
import static org.slf4j.LoggerFactory.getLogger;

final class SessionsImpl {

    private static final Logger log = getLogger("javac");

    static Session session(String source, Path tempDirectory) {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fm = compiler.getStandardFileManager(null, Locale.ROOT, UTF_8);

        var tmp = tempDirectory == null ? tmp() : tempDirectory;
        var srcDir = createTemp(tmp, "src");
        var classOut = createTemp(tmp, "classes");
        var srcOut = createTemp(tmp, "gen-src");

        var src = source.trim();
        var fqName = derive(src);
        var file = fqName.replace('.', '/') + ".java";
        var sourceFile = writeSource(srcDir, file, src);

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
                        var diagnosticString = diagnostic.toString();
                        if (diagnosticString.contains("warning: Supported source version")) {
                            return;
                        }
                        log.warn(diagnosticString);
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
            copySources(srcDir, srcOut);
            return new SessionImpl(
                fqName,
                src,
                srcOut,
                classOut,
                urlClassLoader,
                null
            );
        } catch (Exception e) {
            return new SessionImpl(
                fqName,
                src,
                srcOut,
                classOut,
                urlClassLoader,
                e
            );
        }
    }

    private SessionsImpl() {
    }

    private static final Pattern PACKAGE = Pattern.compile("^package ([\\p{Alnum}.]+)\\s*;\\s*");

    private static Path tmp() {
        try {
            return Files.createTempDirectory(UUID.randomUUID().toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp dir", e);
        }
    }

    private static Path createTemp(Path tempDirectory, String subDir) {
        var resolved = tempDirectory.resolve(subDir);
        try {
            return Files.createDirectories(resolved);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create temp dir " + resolved, e);
        }
    }

    private static void copySources(Path srcDir, Path srcOut) {
        try (var sourceWalk = Files.walk(srcDir)) {
            sourceWalk
                .filter(Files::isRegularFile)
                .forEach(sourcePath -> {
                        var target = srcOut.resolve(srcDir.relativize(sourcePath));
                        try {
                            Files.copy(sourcePath, target, REPLACE_EXISTING);
                        } catch (Exception e) {
                            throw new IllegalStateException(
                                "Could not copy " + sourcePath + " to " + target,
                                e
                            );
                        }
                    }
                );
        } catch (Exception e) {
            throw new IllegalStateException("Could not copy sources", e);
        }
    }

    private static String derive(String source) {
        var lines = source.lines()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
        var packageName =
            partName(lines, "package ", ';');
        var className =
            partName(lines, "public record ", '(')
                .or(() -> partName(lines, "record ", '('))
                .orElseThrow(() ->
                    new IllegalStateException("No record class could be parsed"));
        return packageName.map(pn ->
                "%s.%s".formatted(pn, className))
            .orElse(className);
    }

    private static Optional<String> partName(List<String> lines, String prefix, char stop) {
        return lines.stream()
            .filter(line -> line.startsWith(prefix))
            .map(line ->
                line.substring(prefix.length()).trim())
            .map(suffix ->
                suffix.substring(0, suffix.indexOf(stop)))
            .findFirst();
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

    record SessionImpl(
        String fqName,
        String source,
        Path generatedDir,
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
        public Optional<Path> packageDir() {
            return Arrays.stream(source.split("\n")).flatMap(line ->
                    Stream.of(PACKAGE.matcher(line))
                        .filter(Matcher::matches)
                        .map(matcher ->
                            matcher.group(1))
                        .map(packidge ->
                            packidge.replace('.', '/'))
                        .map(Path::of))
                .findFirst();
        }

        @Override
        public List<Path> generatedFiles() {
            try (var list = Files.walk(generatedDir)) {
                return list
                    .filter(Files::isRegularFile)
                    .filter(path ->
                        path.getFileName().toString().endsWith(".java"))
                    .toList();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to list generated files in " + generatedDir, e);
            }
        }

        @Override
        public Path generatedFile(Path path) {
            return generatedDir.relativize(path);
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
