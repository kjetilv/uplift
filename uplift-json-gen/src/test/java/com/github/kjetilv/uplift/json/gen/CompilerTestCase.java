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
        var source = PATTERN.matcher(java)
            .replaceAll(MessageFormat.format(".{0};\n", testName()));
        session = Session.create(source);
        assertThat(session)
            .describedAs("Could not initialize session")
            .isNotNull();
        assertThat(session.compilationFailed())
            .describedAs("Compilation failed:\n%s", session == null ? "N/A" : session.compileError())
            .isFalse();
        var object = session.readAndVerify(json);
        assertThat(object).isNotNull();
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
        Optional.ofNullable(session.compileError())
            .or(context::getExecutionException)
            .ifPresentOrElse(
                compileError -> {
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
                    link();
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
        var dir = session.generatedDir();
        try (
            var list = Files.list(dir);
        ) {
            var topPackage = list
                .findFirst().orElseThrow(() ->
                    new IllegalStateException("No top package in " + dir)).getFileName();
            var link = Path.of("build/generated/sources/annotationProcessor/java/main/").resolve(topPackage);
            var target = session.generatedDir().resolve(topPackage);
            Files.deleteIfExists(link);
            Files.createSymbolicLink(link, target);
            log.info("Link: {}", link.toUri());
        } catch (Exception e) {
            log.warn("Failed to link: {}", e.toString());
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
