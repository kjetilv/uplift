package com.github.kjetilv.uplift.json.gen.test;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
class SingleFieldCompilerTest extends CompilerTestCase {

    @Test
    void stringFields() {
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
    void bigDecimalFieldsKeepPrecision() {
        verifyValuesKept(//language=java
            """
                public record SingleField(BigDecimal bd) {
                }
                """,
            //language=json
            """
                {
                  "bd": 0.1000000000000000055511151231257827021181583404541015625
                }
                """,
            //language=json
            """
                {
                  "bd": 123456789012345678901234567890
                }
                """
        );
    }

    @Test
    void bigIntegerFieldsBeyondLong() {
        verifyValuesKept(//language=java
            """
                public record SingleField(BigInteger bi) {
                }
                """,
            //language=json
            """
                {
                  "bi": 123456789012345678901234567890
                }
                """,
            //language=json
            """
                {
                  "bi": -9223372036854775809
                }
                """
        );
    }

    @Test
    void integerFieldsAtLimits() {
        verifyValuesKept(//language=java
            """
                public record SingleField(int i, long l, short s, byte b) {
                }
                """,
            //language=json
            """
                { "i": -2147483648, "l": 9223372036854775807, "s": -32768, "b": 127 }
                """,
            //language=json
            """
                { "i": 2147483647, "l": -9223372036854775808, "s": 32767, "b": -128 }
                """
        );
    }

    @Test
    void integerFieldsRejectOutOfRange() {
        var java = //language=java
            """
                public record SingleField(int i, long l, short s, byte b) {
                }
                """;
        verifyReadFails(java, "{ \"i\": 2147483648 }", "does not fit in int");
        verifyReadFails(java, "{ \"l\": 9223372036854775808 }", "does not fit in long");
        verifyReadFails(java, "{ \"s\": 32768 }", "does not fit in short");
        verifyReadFails(java, "{ \"b\": 128 }", "does not fit in byte");
    }

    @Test
    void integerFieldsRejectFractions() {
        var java = //language=java
            """
                public record SingleField(int i, long l, short s, byte b) {
                }
                """;
        verifyReadFails(java, "{ \"i\": 1.5 }", "does not fit in int");
        verifyReadFails(java, "{ \"l\": 1.5 }", "does not fit in long");
        verifyReadFails(java, "{ \"s\": 1.5 }", "does not fit in short");
        verifyReadFails(java, "{ \"b\": 1.5 }", "does not fit in byte");
    }

    @Test
    void integerFieldsAcceptIntegralDecimals() {
        verifyReadsAs(//language=java
            """
                public record SingleField(int i, long l) {
                }
                """,
            //language=json
            """
                { "i": 1.0, "l": 2e3 }
                """,
            //language=json
            """
                { "i": 1, "l": 2000 }
                """
        );
    }

    @Test
    void bigIntegerFields() {
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
