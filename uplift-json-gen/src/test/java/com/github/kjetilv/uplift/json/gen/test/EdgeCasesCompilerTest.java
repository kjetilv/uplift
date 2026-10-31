package com.github.kjetilv.uplift.json.gen.test;

import org.junit.jupiter.api.Test;

public class EdgeCasesCompilerTest extends CompilerTestCase {

    @Test
    void renamedField() {
        verifyRoundTrip(//language=java
            """
                public record Renamed(
                    @com.github.kjetilv.uplift.json.anno.Field("first-name") String firstName,
                    int age
                ) {
                }
                """,
            //language=json
            """
                {
                  "first-name": "Foo",
                  "age": 42
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void renamedSingularList() {
        verifyRoundTrip(//language=java
            """
                public record Parent(
                    @com.github.kjetilv.uplift.json.anno.Field("kids")
                    @com.github.kjetilv.uplift.json.anno.Singular("kid")
                    List<String> children
                ) {
                }
                """,
            //language=json
            """
                {
                  "kids": ["foo", "bar"]
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void twoPrimitiveArraysOfSameType() {
        verifyRoundTrip(//language=java
            """
                public record TwoArrays(int[] a, int[] b) {

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof TwoArrays(var oa, var ob) && Arrays.equals(a, oa) && Arrays.equals(b, ob);
                    }

                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(a) + 31 * Arrays.hashCode(b);
                    }
                }
                """,
            //language=json
            """
                {
                  "a": [1, 2],
                  "b": [3]
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void enumArray() {
        verifyRoundTrip(//language=java
            """
                public record EnumArray(HttpClient.Version[] versions) {

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof EnumArray(var oversions) && Arrays.equals(versions, oversions);
                    }

                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(versions);
                    }
                }
                """,
            //language=json
            """
                {
                  "versions": ["HTTP_1_1", "HTTP_2"]
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void uuidArray() {
        verifyRoundTrip(//language=java
            """
                public record UuidArray(UUID[] ids) {

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof UuidArray(var oids) && Arrays.equals(ids, oids);
                    }

                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(ids);
                    }
                }
                """,
            //language=json
            """
                {
                  "ids": ["0fc4fcf0-8f4c-4bbd-9d26-0a8b28d66e3b"]
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void instantArray() {
        verifyRoundTrip(//language=java
            """
                public record InstantArray(Instant[] times) {

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof InstantArray(var otimes) && Arrays.equals(times, otimes);
                    }

                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(times);
                    }
                }
                """,
            //language=json
            """
                {
                  "times": [1000, 2000]
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void charFields() {
        verifyRoundTrip(//language=java
            """
                public record Chars(char c, Character boxed) {
                }
                """,
            //language=json
            """
                {
                  "c": "x",
                  "boxed": "y"
                }
                """,
            //language=json
            """
                {
                  "c": "x"
                }
                """
        );
    }

    @Test
    void charArray() {
        verifyRoundTrip(//language=java
            """
                public record CharArray(char[] cs) {

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof CharArray(var ocs) && Arrays.equals(cs, ocs);
                    }

                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(cs);
                    }
                }
                """,
            //language=json
            """
                {
                  "cs": ["a", "b"]
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void typeAnnotatedComponents() {
        verifyRoundTrip(//language=java
            """
                public record Annotated(
                    Annotated.@Annotated.Tag Inner inner,
                    List<@Annotated.Tag String> strings,
                    @Annotated.Tag String string
                ) {

                    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)
                    @interface Tag {
                    }

                    record Inner(String foo) {
                    }
                }
                """,
            //language=json
            """
                {
                  "inner": {
                    "foo": "bar"
                  },
                  "strings": ["foo"],
                  "string": "zot"
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void setIsRejected() {
        verifyCompilationFails(//language=java
            """
                public record WithSet(Set<String> strings) {
                }
                """,
            "only java.util.List is supported"
        );
    }

    @Test
    void unknownFieldsOfSameLengthAreSkipped() {
        verifyReadsAs(//language=java
            """
                public record Known(String foo, String barbaz) {
                }
                """,
            //language=json
            """
                {
                  "foo": "yes",
                  "fxx": "no",
                  "barbaz": "yes",
                  "bXXbaz": "no"
                }
                """,
            //language=json
            """
                {
                  "foo": "yes",
                  "barbaz": "yes"
                }
                """
        );
    }
}
