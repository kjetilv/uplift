package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.CONCURRENT)
public class CompilerTestCase {

    private static final Logger log = LoggerFactory.getLogger(CompilerTestCase.class);

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    @RegisterExtension
    private AfterEachCallback afterTestExecutionCallback = this::afterExecutionCallback;

    private Session session;

    private TestInfo testInfo;

    @BeforeEach
    void update(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    protected void ver(String java, String json) {
        session = compilerSession(java);
        assertThat(session)
            .describedAs("Could not initialize session")
            .isNotNull();
        assertThat(session.compilationFailed())
            .describedAs("Compilation failed:\n%s", session == null ? "N/A" : session.compileError())
            .isFalse();
        var object = session.readAndVerify(json);
        assertThat(object).isNotNull();
    }

    private Session compilerSession(String java) {
        var source = PATTERN.matcher(java)
            .replaceAll(MessageFormat.format(".{0};\n", testName()));
        return Session.create(source);
    }

    private String testName() {
        return PARS.matcher(testInfo.getDisplayName())
            .replaceAll("")
            .toLowerCase(Locale.ROOT);
    }

    private void afterExecutionCallback(ExtensionContext context) {
        var generatedFiles = Stream.ofNullable(session)
            .map(Session::generatedFiles)
            .flatMap(List::stream)
            .sorted(Comparator.comparing(path -> {
                try {
                    return Files.getLastModifiedTime(path);
                } catch (Exception e) {
                    return FileTime.from(Instant.EPOCH);
                }
            }))
            .toList();
        link();
        Optional.ofNullable(session)
            .map(Session::compileError)
            .or(context::getExecutionException)
            .ifPresentOrElse(
                compileError -> {
                    log.error("Compilation failed", compileError);
                    if (session == null) {
                        log.warn("Session not created, aborting print phase");
                        return;
                    }
                    log.info("Generated files failed: {}", session.generatedFilesDir().toUri());
                    Stream.iterate(compileError, Objects::nonNull, Throwable::getCause)
                        .forEach(cause -> {
                            var top = Arrays.stream(cause.getStackTrace())
                                .takeWhile(beforeCutoff(context))
                                .toArray(StackTraceElement[]::new);
                            cause.setStackTrace(top);
                        });

                    generatedFiles.stream()
                        .filter(causes(compileError))
                        .findFirst()
                        .ifPresentOrElse(
                            offendingFile -> {
                                print(offendingFile);
                                log.error("That didn't work!", compileError);
                            },
                            () -> {
                                generatedFiles.forEach(this::print);
                                log.error(
                                    "That didn't work! Not sure where the error occurred, {} files generated",
                                    generatedFiles.size(),
                                    compileError
                                );
                            }
                        );
                    log.info("Generated files failed: {}", session.generatedFilesDir().toUri());
                },
                () -> {
                    generatedFiles.forEach(this::print);
                    log.info("That worked out nicely");
                }
            );
        session = null;
        afterTestExecutionCallback = null;
    }

    private void link() {
        if (session == null) {
            log.warn("Session not created, aborting link phase");
            return;
        }
        ;
        try {
            var tempFiles = session.generatedDir();
            var localCopy = Path.of("build/generated/sources/annotationProcessor/java/main/");
            var localCopyPackage = session.packageDir()
                .map(localCopy::resolve)
                .map(CompilerTestCase::createDirs)
                .orElse(localCopy);
            var tempfilesPackage = session.packageDir()
                .map(tempFiles::resolve).orElse(tempFiles);
            try (
                var files = Files.walk(localCopyPackage)
                    .filter(Files::isRegularFile)
            ) {
                files.forEach(CompilerTestCase::rm);
            }
            try (
                var dirs = Files.walk(localCopyPackage)
                    .filter(Files::isDirectory).sorted(LONGEST_FIRST)
            ) {
                dirs.forEach(CompilerTestCase::rm);
            }
            try (
                var dirs = Files.walk(tempfilesPackage)
                    .filter(Files::isDirectory)
            ) {
                createDirs(tempfilesPackage, localCopyPackage, dirs);
            }
            try (
                var files = Files.walk(tempfilesPackage)
                    .filter(Files::isRegularFile)
            ) {
                copyFiles(tempfilesPackage, localCopyPackage, files);
            }
            try (var dirs = Files.walk(localCopy)) {
                dirs.filter(Files::isDirectory)
                    .filter(CompilerTestCase::isEmpty)
                    .forEach(CompilerTestCase::rm);
            }
        } catch (Exception e) {
            if (session.compileError() == null) {
                throw new IllegalStateException(e);
            }
            if (e instanceof NoSuchFileException) {
                log.warn("Failed to copy files after compile error: {}", e.toString());
                return;
            }
            var illegalStateException = new IllegalStateException("Failed to link: ", e);
            illegalStateException.addSuppressed(session.compileError());
            throw illegalStateException;
        }
    }

    private void print(Path file) {
        log.info("{}", session.generatedFile(file));
        try (var lines = Files.lines(file)) {
            lines.forEach(line -> IO.println("⏐⏐    " + line));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + file, e);
        }
    }

    private static final Pattern PATTERN = Pattern.compile(".TESTNAME;\n");

    private static final Pattern PARS = Pattern.compile("\\(\\)");

    private static final Comparator<Path> LONGEST_FIRST = Comparator.comparing(Path::getNameCount).reversed();

    private static boolean isEmpty(Path dir) {
        try (var list = Files.list(dir)) {
            return list.findAny().isEmpty();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list " + dir, e);
        }
    }

    private static void rm(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete " + path, e);
        }
    }

    private static void createDirs(Path source, Path target, Stream<Path> paths) {
        paths.map(source::relativize)
            .map(target::resolve)
            .forEach(CompilerTestCase::createDirs);
    }

    private static Path createDirs(Path path) {
        try {
            return Files.createDirectories(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create " + path, e);
        }
    }

    private static void copyFiles(Path source, Path target, Stream<Path> paths) {
        paths.forEach(path -> {
            try {
                var copy = target.resolve(source.relativize(path));
                Files.copy(path, copy);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create " + path, e);
            }
        });
    }

    private static Predicate<StackTraceElement> beforeCutoff(ExtensionContext context) {
        return context.getTestMethod()
            .map(method ->
                method.getDeclaringClass().getName() + "." + method.getName())
            .map(cutoff -> {
                AtomicBoolean cutoffSeen = new AtomicBoolean();
                return (Predicate<StackTraceElement>) stackTraceElement -> {
                    if (cutoffSeen.get()) {
                        return false;
                    }
                    cutoffSeen.set(stackTraceElement.toString().contains(cutoff));
                    return true;
                };
            })
            .orElse(_ -> true);
    }

    private static Predicate<Path> causes(Throwable compileError) {
        var message = compileError.toString();
        return path ->
            message.contains(path.getFileName().toString());
    }
}
