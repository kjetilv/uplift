package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
class CompilerTest extends CompilerTestCase {

    @Test
    void stringFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
    void longFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
    void longListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
    void intListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<Integer> ls) {
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
    void shortFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
    void shortListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<Short> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [1, 2, 3]
                }
                """
        );
    }

    @Test
    void shortPrimitiveFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
    void byteListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<Byte> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [125, 126, 127]
                }
                """
        );
    }

    @Test
    void bytePrimitiveFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
    void floatListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<Float> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [1.1, 1.2, 1.3]
                }
                """
        );
    }

    @Test
    void floatPrimitiveFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
    void doubleListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<Double> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [100.1, 100.2, 100.3]
                }
                """
        );
    }

    @Test
    void doublePrimitiveFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
    void bigDecimalListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<java.math.BigDecimal> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [100.1, 100.2, 100.3]
                }
                """
        );
    }

    @Test
    void bigIntegerFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
    void bigIntegerListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<java.math.BigInteger> ls) {
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
    void uuidFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.UUID id) {
                }
                """,
            //language=json
            """
                {
                  "id": "%s"
                }
                """.formatted(UUID.randomUUID())
        );
    }

    @Test
    void uuidListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<java.util.UUID> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": ["%s", "%s"]
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID())
        );
    }

    @Test
    void booleanFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
    void booleanPrimitiveFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(boolean b) {
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
    void booleanListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(java.util.List<Boolean> ls) {
                }
                """,
            //language=json
            """
                {
                  "ls": [false, false, true]
                }
                """
        );
    }

    @Test
    void simpleCase() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
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
        ver(//language=java
            """
                package prince.little.TESTNAME;
                
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

    @Test
    void emptyMaps() {
        ver(//language=java
            """
                package prince.little.TESTNAME;
                
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
                    }
                }
                """
        );
    }

    @Test
    void nestedType() {
        ver(//language=java
            """
                package nest.eagles.TESTNAME;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record Eagle(Nest nest) {
                    record Nest(String foo) {}
                }
                """,
            //language=json
            """
                {
                  "nest": {
                    "foo": "bar"
                  }
                }
                """);
    }
}
