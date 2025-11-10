package com.github.kjetilv.uplift.json.match;

import org.junit.jupiter.api.Test;

import java.util.Map;

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
            Match<Object> match = matcher(subset).match(mask);
            System.out.println("\nPathways\n");
            match.pathways().stream()
                .flatMap(probe -> probe.lines("", "  "))
                .forEach(System.out::println);
            System.out.println("\nStructure\n");
            System.out.println("  " + main);
            System.out.println("\nMask\n");
            System.out.println("  " + mask);
            System.out.println("\nSubset\n");
            System.out.println("  " + subset);
            System.out.println("\nLeaves\n");
            match.leaves()
                .map(l -> "  " + l)
                .forEach(System.out::println);
        });

        Map<Pointer<Object>, Diff<Object>> subdiff = differ.subdiff(mask);
//        System.out.println(subdiff);

        subdiff.forEach((jsonNodePointer, jsonNodeDiff) ->
            System.out.println(jsonNodePointer + " -> " + jsonNodeDiff));

        assertThat(differ.diff(mask)).hasValueSatisfying(System.out::println);
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
