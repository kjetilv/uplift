package com.github.kjetilv.uplift.json.gen.test;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
public class PrimitiveFieldCompilerTest extends CompilerTestCase {

    @Test
    void longPrimitiveFields() {
        verifyRoundTrip(//language=java
            """
                public record SingleField(long l) {
                }
                """,
            //language=json
            """
                {
                  "l": 1000
                }
                """,
            //language=json
            """
                {
                  "l": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void longFields() {
        verifyRoundTrip(//language=java
            """
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
    void intPrimitiveFields() {
        verifyRoundTrip(//language=java
            """
                public record SingleField(int i) {
                }
                """,
            //language=json
            """
                {
                  "i": 576
                }
                """,
            //language=json
            """
                {
                  "i": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void shortPrimitiveFields() {
        verifyRoundTrip(//language=java
            """
                public record SingleField(short s) {
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
    void bytePrimitiveFields() {
        verifyRoundTrip(//language=java
            """
                public record SingleField(byte b) {
                }
                """,
            //language=json
            """
                {
                  "b": 32
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void floatPrimitiveFields() {
        verifyRoundTrip(//language=java
            """
                public record SingleField(float f) {
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
    void doublePrimitiveFields() {
        verifyRoundTrip(//language=java
            """
                public record SingleField(double d) {
                }
                """,
            //language=json
            """
                {
                  "d": 3.14
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void booleanPrimitiveFields() {
        verifyRoundTrip(//language=java
            """
                public record SingleField(boolean b) {
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
                  "b": false
                }
                """,
            //language=json
            "{}"
        );
    }

}
