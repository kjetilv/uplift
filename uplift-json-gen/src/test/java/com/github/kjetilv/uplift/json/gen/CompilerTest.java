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
        session = Session.create(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Long l) {}
                """
        );
        assertThat(session.readAndVerify(
            //language=json
            """
                {"l": 1000}
                """
        )).isNotNull();
    }

    @Test
    void intFields() {
        session = Session.create(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Integer i) {}
                """
        );
        assertThat(session.readAndVerify(
            //language=json
            """
                {"i": 576}
                """
        )).isNotNull();
    }

    @Test
    void shortFields() {
        session = Session.create(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Short s) {}
                """
        );
        assertThat(session.readAndVerify(
            //language=json
            """
                {"s": 42}
                """
        )).isNotNull();
    }

    @Test
    void byteFields() {
        session = Session.create(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Byte b) {}
                """
        );
        assertThat(session.readAndVerify(
            //language=json
            """
                {"b": 32}
                """
        )).isNotNull();
    }

    @Test
    void floatFields() {
        session = Session.create(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Float f) {}
                """
        );
        assertThat(session.readAndVerify(
            //language=json
            """
                {"f": 3.14}
                """
        )).isNotNull();
    }

    @Test
    void doubleFields() {
        session = Session.create(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Double d) {}
                """
        );
        assertThat(session.readAndVerify(
            //language=json
            """
                {"d": 3.14}
                """
        )).isNotNull();
    }

    @Test
    void bigDecimalFields() {
        session = Session.create(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.math.BigDecimal bd) {}
                """
        );
        assertThat(session.readAndVerify(
            //language=json
            """
                {"bd": 3.14}
                """
        )).isNotNull();
    }

    @Test
    void booleanFields() {
        session = Session.create(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Boolean b) {}
                """
        );
        assertThat(session.readAndVerify(
            //language=json
            """
                {"b": true}
                """
        )).isNotNull();
    }

    @Test
    void simpleCase() {
        var session = Session.create(
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
        assertThat(session.readAndVerify(
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

    private void afterExecutionCallback(ExtensionContext context) {
        Optional.ofNullable(session.compileError())
            .or(context::getExecutionException)
            .ifPresentOrElse(
                exception -> {
                    log.info("That didn't work!", exception);
                },
                () -> log.info("Success!")
            );
        if (session != null) session.generatedFiles()
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
