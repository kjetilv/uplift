package com.github.kjetilv.uplift.json.gen.test;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
public class RicherCasesCompilerTest extends CompilerTestCase {

    @Test
    void simpleCase() {
        verifyRoundTrip(//language=java
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
        verifyRoundTrip(//language=java
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
                    "map": null
                }
                """
        );
    }

    @Test
    void emptyMapsAreNull() {
        var java = //language=java
            """
                public record FooMap(String before, Map<String, ?> map) {
                }
                """;
        verifyWritten(java, """
            {"before": "b", "map": {}}
            """, """
            {"before": "b"}
            """);
        verifyWritten(java, """
            {"before": "b", "map": {"a": null, "b": {}, "c": {"d": null}}}
            """, """
            {"before": "b"}
            """);
    }

    @Test
    void nestedMaps() {
        verifyWritten(//language=java
            """
                public record FooMap(String before, Map<String, ?> map, String after) {
                }
                """,
            //language=json
            """
                {
                    "before": "b",
                    "map": {
                      "a": "x",
                      "nested": {
                        "n": 1,
                        "deeper": {
                          "t": true
                        },
                        "empty": {}
                      },
                      "z": 2
                    },
                    "after": "a"
                }
                """,
            //language=json
            """
                {
                    "before": "b",
                    "map": {
                      "a": "x",
                      "nested": {
                        "n": 1,
                        "deeper": {
                          "t": true
                        }
                      },
                      "z": 2
                    },
                    "after": "a"
                }
                """
        );
    }

    @Test
    void mapsWithNullsAndLists() {
        verifyWritten(//language=java
            """
                public record FooMap(String before, Map<String, ?> map, String after) {
                }
                """,
            //language=json
            """
                {
                    "before": "b",
                    "map": {
                      "nothing": null,
                      "list": [1, "two", 3.5, true, null],
                      "empty": [],
                      "objects": [{"k": "v", "n": null}, {}, {"n": null}],
                      "lists": [[1, 2], ["a\\"b"]],
                      "nested": {
                        "list": [{"deep": [false]}],
                        "nothing": null,
                        "allNull": {"x": null, "y": {"z": null}}
                      }
                    },
                    "after": "a"
                }
                """,
            //language=json
            """
                {
                    "before": "b",
                    "map": {
                      "list": [1, "two", 3.5, true, null],
                      "empty": [],
                      "objects": [{"k": "v"}, null, null],
                      "lists": [[1, 2], ["a\\"b"]],
                      "nested": {
                        "list": [{"deep": [false]}]
                      }
                    },
                    "after": "a"
                }
                """
        );
    }
}
