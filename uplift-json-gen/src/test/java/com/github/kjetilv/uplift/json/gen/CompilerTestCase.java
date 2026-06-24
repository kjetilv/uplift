package com.github.kjetilv.uplift.json.gen;

import module java.base;
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

    protected void verify(String java, String json) {
        session = Session.create(java);
        assertThat(session)
            .describedAs("Could not initialize session")
            .isNotNull();
        assertThat(session.compilationFailed())
            .describedAs("Compilation failed:\n%s", session == null ? "N/A" : session.compileError())
            .isFalse();
        var object = Objects.requireNonNull(session, "session").readAndVerify(json);
        assertThat(object).isNotNull();
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
                },
                () -> {
                    generatedFiles.forEach(this::print);
                    log.info("That worked out nicely");
                }
            );
        session = null;
        afterTestExecutionCallback = null;
    }

    private void print(Path file) {
        log.info("{}", session.generatedFile(file));
        try (var lines = Files.lines(file)) {
            lines.forEach(line -> IO.println("⏐⏐    " + line));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + file, e);
        }
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
