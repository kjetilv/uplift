package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
public class RicherCasesCompilerTest extends CompilerTestCase {

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
}
