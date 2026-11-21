package com.github.kjetilv.uplift.json.match.test;

import com.github.kjetilv.uplift.json.match.Diff;
import com.github.kjetilv.uplift.json.match.MapsStructure;
import com.github.kjetilv.uplift.json.match.Pointer;
import com.github.kjetilv.uplift.json.match.Structures;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(diff).containsExactly(Map.entry(
            Pointer.node("foo", Pointer.leaf(), new MapsStructure()),
            new Diff<>(42, "bar")
        ));
        IO.println(diff);
        differ.diff(json2)
            .ifPresent(IO::println);
    }
}
