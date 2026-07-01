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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<String> ss) {
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<Long> ls) {
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<Integer> ls) {
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<Short> ls) {
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import com.github.kjetilv.uplift.json.anno.JsonRecord;
                
                @JsonRecord
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<Byte> ls) {
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<Float> ls) {
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<Double> ls) {
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(BigDecimal bd) {
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<BigDecimal> ls) {
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(BigInteger bi) {
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<BigInteger> ls) {
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(UUID id) {
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<UUID> ls) {
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
    void enumFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                import module java.base;
                import module java.net.http;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(HttpClient.Version version) {
                }
                """,
            //language=json
            """
                {
                  "version": "HTTP_1_1"
                }
                """);
    }

    @Test
    void enumListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                import module java.base;
                import module java.net.http;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record ListField(List<HttpClient.Version> version) {
                }
                """,
            //language=json
            """
                {
                  "version": ["HTTP_1_1", "HTTP_2"]
                }
                """);
    }

    @Test
    void durationFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(Duration dura) {
                }
                """,
            //language=json
            """
                {
                  "dura": "PT50S"
                }
                """);
    }

    @Test
    void durationListFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<Duration> ds) {
                }
                """,
            //language=json
            """
                {
                  "ds": ["PT42S", "PT54S"]
                }
                """
        );
    }

    @Test
    void booleanFields() {
        ver(//language=java
            """
                package junker.barabas.TESTNAME;
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                import module java.base;

                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record SingleField(List<Boolean> ls) {
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record FooMap(Map<String, ?> map) {
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
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record FooMap(Map<String, ?> map) {
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
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
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

    @Test
    void nestedTypeEmpty() {
        ver(//language=java
            """
                package nest.eagles.TESTNAME;
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record Eagle(Nest nest) {
                    record Nest(String foo) {}
                }
                """,
            //language=json
            """
                {
                  "nest": {
                  }
                }
                """);
    }

    @Test
    void nestedTypeNull() {
        ver(//language=java
            """
                package nest.eagles.TESTNAME;
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record Eagle(Nest nest) {
                    record Nest(String foo) {}
                }
                """,
            //language=json
            "{}");
    }

    @Test
    void nestedTypeList() {
        ver(//language=java
            """
                package nest.eagles.TESTNAME;
                
                import module java.base;
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record Eagle(List<Nest> nests) {
                    record Nest(String foo) {}
                }
                """,
            //language=json
            """
                {
                  "nests": [
                    {
                       "foo": "bar"
                    },
                    {
                       "zot": "zip"
                    }
                  ]
                }"""
        );
    }

    @Test
    void compelexNestedTypeList() {
        ver(//language=java
            """
                package nest.eagles.TESTNAME;
                
                import module java.base;
                
                import com.github.kjetilv.uplift.json.anno.*;
                
                @JsonRecord
                public record Eagle(List<Nest> nests) {
                    record Nest(String foo, List<Integer> nums) {}
                }
                """,
            //language=json
            """
                {
                  "nests": [
                    {
                       "foo": "bar",
                       "nums":[1,2]
                    },
                    {
                       "foo": "zot",
                       "nums": [42, 54]
                    }
                  ]
                }
                """
        );
    }
}
