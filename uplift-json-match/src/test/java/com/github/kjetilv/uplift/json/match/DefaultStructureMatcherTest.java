package com.github.kjetilv.uplift.json.match;

import org.junit.jupiter.api.Test;

import java.util.Map;

class DefaultStructureMatcherTest {

    @Test
    void diff1() {
        var json1 = JsonDings.json(
            //language=json
            """
                {
                  "foo": "bar",
                  "zot": true
                }
                """);
        var json2 = JsonDings.json(
            //language=json
            """
                {
                  "foo": 42,
                  "zot": true
                }
                """);

        var differ = Structures.differ(json1, Structures.MAPS);
        var diff = differ.subdiff(json2);
        System.out.println(diff);
        differ.diff(json2).ifPresent(System.out::println);
    }
}