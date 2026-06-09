package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
class CompilerTest {

    @Test
    void floatFields() {
        var session = Sessions.create(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Float f) {}
                """
        );
        session.readAndVerify(
            //language=json
            """
                {"f": 3.14}"""
        );
    }

    @Test
    void doubleFields() {
        var session = Sessions.create(
            "junker.barabas.SingleField",
            //language=java
            """
                package junker.barabas;
                
                @com.github.kjetilv.uplift.json.anno.JsonRecord
                public record SingleField(Double d) {}
                """
        );
        session.readAndVerify(
            //language=json
            """
                {"d": 3.14}"""
        );
    }

    @Test
    void simpleCase() {
        var session = Sessions.create(
            "junker.barabas.Foo",
            //language=java
            """
                package junker.barabas;
                
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
                """
        );
        session.readAndVerify(
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
}
