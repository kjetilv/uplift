package com.github.kjetilv.uplift.json.match;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonExtractorTest {

    private StructureExtractor<Object> subsetMatcher;

    @Test
    void canExtractSimple() {
        var main = JsonDings.json(
            """
                {
                  "foo": {
                    "bar": {
                      "zot": 42,
                      "arrs": [
                        {
                          "key": "a",
                          "val": 1
                        },
                        {
                          "key": "b",
                          "val":2
                        }
                      ]
                    },
                    "zip": true,
                    "more": "stuff"
                  },
                  "qos": 1.5
                }
                """);

        var structureMatcher = matcher(main);
        var extractor = extractor(main);
        var matcher = structureMatcher;
        var differ = differ(main);

        var mask = JsonDings.json(
            """
                {
                  "foo": {
                    "bar": {
                      "zot": 43,
                      "arrs":
                        [
                          null,
                          {
                            "key": "a",
                            "val": 2
                          },
                          {
                          }
                        ]
                    },
                    "zip": true
                  },
                  "qos": 1.5
                }
                """
        );
        var extract = extractor.extract(mask);
        assertThat(extract).hasValueSatisfying(subset -> {
            assertThat(matcher.contains(subset)).isTrue();
            var match = matcher(subset).match(mask);
            IO.println("\nPathways\n");
            match.pathways().stream()
                .flatMap(probe -> probe.lines("", "  "))
                .forEach(IO::println);
            IO.println("\nStructure\n");
            IO.println("  " + main);
            IO.println("\nMask\n");
            IO.println("  " + mask);
            IO.println("\nSubset\n");
            IO.println("  " + subset);
            IO.println("\nLeaves\n");
            match.leaves()
                .map(l -> "  " + l)
                .forEach(IO::println);
        });

        var subdiff = differ.subdiff(mask);
//        IO.println(subdiff);

        subdiff.forEach((jsonNodePointer, jsonNodeDiff) ->
            IO.println(jsonNodePointer + " -> " + jsonNodeDiff));

        assertThat(differ.diff(mask)).hasValueSatisfying(IO::println);
    }

    private static StructureExtractor<Object> extractor(Object main) {
        return Structures.extractor(main, Structures.MAPS);
    }

    private static StructureDiffer<Object> differ(Object main) {
        return Structures.differ(main, Structures.MAPS);
    }

    private static StructureMatcher<Object> matcher(Object main) {
        return Structures.matcher(main, Structures.MAPS, Structures.ArrayStrategy.EXACT);
    }
}
