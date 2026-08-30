package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
class SingleFieldCompilerTest extends CompilerTestCase {

    @Test
    void stringFields() {
        ver(//language=java
            """
                public record SingleField(String s) {
                }
                """,
            //language=json
            """
                {
                  "s": "foo"
                }
                """,
            //language=json
            """
                {
                  "s": ""
                }
                """,
            //language=json
            """
                {
                  "s": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void intFields() {
        ver(//language=java
            """
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
    void shortFields() {
        ver(//language=java
            """
                public record SingleField(Short s) {
                }
                """,
            //language=json
            """
                {
                  "s": 42
                }
                """,
            //language=json
            """
                {
                  "s": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void byteFields() {
        ver(//language=java
            """
                public record SingleField(Byte b) {
                }
                """,
            //language=json
            """
                {
                  "b": 32
                }
                """,
            //language=json
            """
                {
                  "b": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void floatFields() {
        ver(//language=java
            """
                public record SingleField(Float f) {
                }
                """,
            //language=json
            """
                {
                  "f": 3.14
                }
                """,
            //language=json
            """
                {
                  "f": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void doubleFields() {
        ver(//language=java
            """
                public record SingleField(Double d) {
                }
                """,
            //language=json
            """
                {
                  "d": 3.14
                }
                """,
            //language=json
            """
                {
                  "d": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void bigDecimalFields() {
        ver(//language=java
            """
                public record SingleField(BigDecimal bd) {
                }
                """,
            //language=json
            """
                {
                  "bd": 3.14
                }
                """,
            //language=json
            """
                {
                  "bd": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void bigIntegerFields() {
        ver(//language=java
            """
                public record SingleField(BigInteger bi) {
                }
                """,
            //language=json
            """
                {
                  "bi": 32
                }
                """,
            //language=json
            """
                {
                  "bi": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void uuidFields() {
        ver(//language=java
            """
                public record SingleField(UUID id) {
                }
                """,
            //language=json
            """
                {
                  "id": "%s"
                }
                """.formatted(UUID.randomUUID()),
            //language=json
            "{}"
        );
    }

    @Test
    void enumFields() {
        ver(//language=java
            """
                public record SingleField(HttpClient.Version version) {
                }
                """,
            //language=json
            """
                {
                  "version": "HTTP_1_1"
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void durationFields() {
        ver(//language=java
            """
                public record SingleField(Duration dura) {
                }
                """,
            //language=json
            """
                {
                  "dura": "PT50S"
                }
                """,
            //language=json
            """
                {
                  "dura": "PT0S"
                }
                """,
            //language=json
            """
                {
                  "dura": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void booleanFields() {
        ver(//language=java
            """
                public record SingleField(Boolean b) {
                }
                """,
            //language=json
            """
                {
                  "b": true
                }
                """,
            //language=json
            """
                {
                  "b": null
                }
                """,
            //language=json
            "{}"
        );
    }
}
