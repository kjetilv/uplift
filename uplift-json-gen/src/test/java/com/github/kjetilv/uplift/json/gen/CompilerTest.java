package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ClassNameDiffersFromFileName")
class CompilerTest {

    private static final Logger log = LoggerFactory.getLogger(CompilerTest.class);

    @SuppressWarnings("unused")
    @RegisterExtension
    private final AfterEachCallback afterTestExecutionCallback = this::afterExecutionCallback;

    private Session session;

    @Test
    void longFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Long l) {
                }
                """,
            //language=json
            """
                {
                  "l": 1000
                }
                """
        );
    }

    @Test
    void stringFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(String s) {
                }
                """,
            //language=json
            """
                {
                  "s": "foo"
                }
                """
        );
    }

    @Test
    void stringListFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<String> ss) {
                }
                """,
            //language=json
            """
                {
                  "ss": ["foo", "bar"]
                }
                """
        );
    }

    @Test
    void longPrimitiveFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(long l) {
                }
                """,
            //language=json
            """
                {
                  "l": 1000
                }
                """
        );
    }

    @Test
    void longListFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<Long> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [1001, 1002, 1003]
                }
                """
        );
    }

    @Test
    void intFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Integer i) {
                }
                """,
            //language=json
            """
                {
                  "i": 576
                }
                """
        );
    }

    @Test
    void intPrimitiveFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(int i) {
                }
                """,
            //language=json
            """
                {
                  "i": 576
                }
                """
        );
    }

    @Test
    void shortFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Short s) {
                }
                """,
            //language=json
            """
                {
                  "s": 42
                }
                """
        );
    }

    @Test
    void shortPrimitiveFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(short s) {
                }
                """,
            //language=json
            """
                {
                  "s": 42
                }
                """
        );
    }

    @Test
    void byteFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Byte b) {
                }
                """,
            //language=json
            """
                {
                  "b": 32
                }
                """
        );
    }

    @Test
    void bytePrimitiveFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(byte b) {
                }
                """,
            //language=json
            """
                {
                  "b": 32
                }
                """
        );
    }

    @Test
    void floatFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Float f) {
                }
                """,
            //language=json
            """
                {
                  "f": 3.14
                }
                """
        );
    }

    @Test
    void floatPrimitiveFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(float f) {
                }
                """,
            //language=json
            """
                {
                  "f": 3.14
                }
                """
        );
    }

    @Test
    void doubleFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Double d) {
                }
                """,
            //language=json
            """
                {
                  "d": 3.14
                }
                """
        );
    }

    @Test
    void doublePrimitiveFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(double d) {
                }
                """,
            //language=json
            """
                {
                  "d": 3.14
                }
                """
        );
    }

    @Test
    void bigDecimalFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.math.BigDecimal bd) {
                }
                """,
            //language=json
            """
                {
                  "bd": 3.14
                }
                """
        );
    }

    @Test
    void bigIntegerFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.math.BigInteger bi) {
                }
                """,
            //language=json
            """
                {
                  "bi": 32
                }
                """
        );
    }

    @Test
    void uuidFields() {
        var uuid = UUID.randomUUID();
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.UUID id) {
                }
                """,
            //language=json
            """
                {
                  "id": "%s"
                }
                """.formatted(uuid)
        );
    }

    @Test
    void booleanFields() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Boolean b) {
                }
                """,
            //language=json
            """
                {
                  "b": true
                }
                """
        );
    }

    @Test
    void simpleCase() {
        verify(
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record Foo(
                    Boolean foo,
                    String zot,
                    Integer five,
                    Long six,
                    Short seven,
                    Byte eight
                ) {
                }
                """,
            //language=json
            """
                {
                    "foo": true,
                    "zot": "zip",
                    "five": 6,
                    "six": 7,
                    "seven": 8,
                    "eight": 2
                }
                """
        );
    }

    @Test
    void maps() {
        verify(
            //language=java
            """
                package prince.little;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record FooMap(
                    java.util.Map<String, ?> map
                ) {
                }
                """,
            //language=json
            """
                {
                    "map": {
                      "foo": "bar"
                    }
                }
                """
        );
    }

    private void verify(String java, String json) {
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
                                log.error("That didn't work! Not sure where the error occurred", compileError);
                            }
                        );
                },
                () -> {
                    generatedFiles.forEach(this::print);
                    log.info("That worked out nicely");
                }
            );
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
