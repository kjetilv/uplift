package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SuppressWarnings("ClassNameDiffersFromFileName")
class CompilerTest {

    private static final Logger log = LoggerFactory.getLogger(CompilerTest.class);

    @SuppressWarnings("unused")
    @RegisterExtension
    private final AfterEachCallback afterTestExecutionCallback = this::afterExecutionCallback;

    private Session session;

    @Test
    void longFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Long l) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"l": 1000}
                """
        )).isNotNull();
    }

    @Test
    void longPrimitiveFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(long l) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"l": 1000}
                """
        )).isNotNull();
    }

    @Test
    void intFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Integer i) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"i": 576}
                """
        )).isNotNull();
    }

    @Test
    void intPrimitiveFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(int i) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"i": 576}
                """
        )).isNotNull();
    }

    @Test
    void shortFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Short s) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"s": 42}
                """
        )).isNotNull();
    }

    @Test
    void shortPrimitiveFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(short s) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"s": 42}
                """
        )).isNotNull();
    }

    @Test
    void byteFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Byte b) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"b": 32}
                """
        )).isNotNull();
    }

    @Test
    void bytePrimitiveFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(byte b) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"b": 32}
                """
        )).isNotNull();
    }

    @Test
    void floatFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Float f) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"f": 3.14}
                """
        )).isNotNull();
    }

    @Test
    void floatPrimitiveFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(float f) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"f": 3.14}
                """
        )).isNotNull();
    }

    @Test
    void doubleFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Double d) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"d": 3.14}
                """
        )).isNotNull();
    }

    @Test
    void doublePrimitiveFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(double d) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"d": 3.14}
                """
        )).isNotNull();
    }

    @Test
    void bigDecimalFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.math.BigDecimal bd) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"bd": 3.14}
                """)
        ).isNotNull();
    }

    @Test
    void bigIntegerFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.math.BigInteger bi) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"bi": 32}
                """)
        ).isNotNull();
    }

    @Test
    void uuidFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.UUID id) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"id": "%s"}
                """.formatted(UUID.randomUUID()))
        ).isNotNull();
    }

    @Test
    void booleanFields() {
        session(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Boolean b) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {"b": true}
                """
        )).isNotNull();
    }

    @Test
    void simpleCase() {
        session(
            "junker.barabas.Foo",
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
                """
        );
        assertThat(read(
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
        )).isNotNull();
    }

    @Test
    void maps() {
        session(
            "prince.little.FooMap",
            //language=java
            """
                package prince.little;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record FooMap(
                    java.util.Map<String, ?> map
                ) {}
                """
        );
        assertThat(read(
            //language=json
            """
                {
                    "map": {
                      "foo": "bar"
                    }
                }
                """
        )).isNotNull();
    }

    private void session(String name, String source) {
        session = Session.create(name, source);
    }

    private Object read(String json) {
        assertThat(session)
            .describedAs("Could not initialize session")
            .isNotNull();
        assertThat(session.compilationFailed())
            .describedAs("Compilation failed:\n%s", session == null ? "N/A" : session.compileError())
            .isFalse();
        try {
            return session.readAndVerify(json);
        } catch (Exception e) {
            return fail(e);
        }
    }

    private void afterExecutionCallback(ExtensionContext context) {
        Optional.ofNullable(session)
            .map(Session::compileError)
            .or(context::getExecutionException)
            .ifPresentOrElse(
                exception -> {
                    log.info("That didn't work!", exception);
                },
                () -> log.info("Success!")
            );
        Stream.ofNullable(session)
            .map(Session::generatedFiles).flatMap(List::stream)
            .forEach(file -> {
                log.info("{}", session.generatedFile(file));
                try (var lines = Files.lines(file)) {
                    lines.forEach(line -> IO.println("⏐⏐    " + line));
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to read " + file, e);
                }
            });
    }
}
