package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
class CompilerTest extends CompilerTestCase {

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
    void stringArrayFields() {
        ver(//language=java
            """
                public record SingleArrayField(String[] ss) {
                
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SingleArrayField(var oss) && Arrays.equals(ss, oss);
                    }
                
                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(ss);
                    }
                }
                """,
            //language=json
            """
                {
                  "ss": ["foo"]
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
                  "s": []
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
    void longPrimitiveArrayFields() {
        ver(//language=java
            """
                public record SingleField(long[] ls) {
                
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SingleField(var ols) && Arrays.equals(ls, ols);
                    }
                
                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(ls);
                    }
                }
                """,
            //language=json
            """
                {
                  "ls": [1000]
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
    void longArrayFields() {
        ver(
            //language=java
            """
                public record SingleField(Long[] ls) {
                
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SingleField(var ols) && Arrays.equals(ls, ols);
                    }
                
                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(ls);
                    }
                }
                """,
            //language=json
            """
                {
                  "ls": [ 42 ]
                }
                """,
            //language=json
            """
                {
                  "ls": [ 42, 1729 ]
                }
                """,
            //language=json
            """
                {
                  "ls": null
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
               {}
               """
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
    void floatPrimitiveArrayFields() {
        ver(//language=java
            """
                public record SingleField(float[] fs) {
                
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SingleField(var ofs) && Arrays.equals(fs, ofs);
                    }
                
                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(fs);
                    }
                }
                """,
            //language=json
            """
                {
                  "ls": [1000.0]
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

    @Test
    void simpleCase() {
        ver(//language=java
            """
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
                  "zot": "zip",
                  "six": 7
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
                """,
            //language=json
            """
                {
                  "foo": false
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void maps() {
        ver(//language=java
            """
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
                """,
            //language=json
            """
                {
                    "map": {}
                }
                """,
            //language=json
            """
                {
                    "map": null
                }
                """
        );
    }

    @Test
    void nestedType() {
        ver(//language=java
            """
                public record Eagle(Nest nest) {
                    record Nest(String foo) {}
                }
                """,
            //language=json
            """
                {
                  "nest": {"foo": "bar"}
                }
                """,
            //language=json
            """
                {
                  "nest": {}
                }
                """,
            //language=json
            """
                {
                  "nest": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void nestedTypeList() {
        ver(//language=java
            """
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
                """,
            //language=json
            """
                {
                  "nests": [
                    {
                       "nums":[1,2]
                    },
                    {
                       "foo": "zot",
                       "nums": []
                    }
                  ]
                }
                """,
            //language=json
            """
                {
                  "nests": [{}, { "foo": "zot" }]
                }
                """,
            //language=json
            """
                {
                  "nests": [{}, {}]
                }
                """,
            //language=json
            """
                {
                  "nests": [{}, null]
                }
                """
            ,
            //language=json
            """
                {
                  "nests": [null, {}]
                }
                """
        );
    }
}
