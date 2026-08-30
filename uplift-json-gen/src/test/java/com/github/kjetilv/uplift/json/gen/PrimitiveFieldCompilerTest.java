package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
public class PrimitiveFieldCompilerTest extends CompilerTestCase {

    @Test
    void longPrimitiveFields() {
        ver(//language=java
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
        ver(//language=java
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
        ver(//language=java
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
        ver(//language=java
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
        ver(//language=java
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
        ver(//language=java
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
        ver(//language=java
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
        ver(//language=java
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
