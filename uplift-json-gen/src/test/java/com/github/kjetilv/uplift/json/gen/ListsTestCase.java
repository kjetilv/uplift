package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
public class ListsTestCase extends CompilerTestCase {

    @Test
    void stringListFields() {
        ver(//language=java
            """
                public record SingleField(List<String> ss) {
                }
                """,
            //language=json
            """
                {
                  "ss": ["foo", "bar"]
                }
                """,
            //language=json
            """
                {
                  "ss": []
                }
                """,
            //language=json
            """
                {
                  "ss": [""]
                }
                """,
            //language=json
            """
                {
                  "ss": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void longListFields() {
        ver(//language=java
            """
                public record SingleField(List<Long> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [1001, 1002, 1003]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void intListFields() {
        ver(//language=java
            """
                public record SingleField(List<Integer> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [1001, 1002, 1003]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void shortListFields() {
        ver(//language=java
            """
                public record SingleField(List<Short> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [1, 2, 3]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void byteListFields() {
        ver(//language=java
            """
                public record SingleField(List<Byte> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [125, 126, 127]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void floatListFields() {
        ver(//language=java
            """
                public record SingleField(List<Float> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [1.1, 1.2, 1.3]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void doubleListFields() {
        ver(//language=java
            """
                public record SingleField(List<Double> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [100.1, 100.2, 100.3]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }
    @Test
    void bigDecimalListFields() {
        ver(//language=java
            """
                public record SingleField(List<BigDecimal> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [100.1, 100.2, 100.3]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }
    @Test
    void bigIntegerListFields() {
        ver(//language=java
            """
                public record SingleField(List<BigInteger> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [1001, 1002, 1003]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }
    @Test
    void uuidListFields() {
        ver(//language=java
            """
                public record SingleField(List<UUID> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": ["%s", "%s"]
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID()),
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            "{}"
        );
    }
    @Test
    void enumListFields() {
        ver(//language=java
            """
                public record ListField(List<HttpClient.Version> version) {
                }
                """,
            //language=json
            """
                {
                  "version": ["HTTP_1_1", "HTTP_2"]
                }
                """,
            //language=json
            """
                {
                  "version": []
                }
                """,
            //language=json
            """
                {
                  "version": null
                }
                """,
            //language=json
            "{}"
        );
    }
    @Test
    void durationListFields() {
        ver(//language=java
            """
                public record SingleField(List<Duration> ds) {
                }
                """,
            //language=json
            """
                {
                  "ds": ["PT42S", "PT54S"]
                }
                """,
            //language=json
            """
                {
                  "ds": []
                }
                """,
            //language=json
            """
                {
                  "ds": null
                }
                """,
            //language=json
            "{}"
        );
    }
    @Test
    void booleanListFields() {
        ver(//language=java
            """
                public record SingleField(List<Boolean> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [false, false, true]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }
}
