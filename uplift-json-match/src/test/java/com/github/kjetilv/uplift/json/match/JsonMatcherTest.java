package com.github.kjetilv.uplift.json.match;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SameParameterValue")
class JsonMatcherTest {

    private StructureMatcher<Object> subsetMatcher1;

    private StructureMatcher<Object> subsetMatcher2;

    private StructureMatcher<Object> exactMatcher1;

    private StructureMatcher<Object> exactMatcher2;

    @BeforeEach
    void setUp() {
        subsetMatcher1 = Structures.matcher(
            JsonDings.json(JSON),
            Structures.MAPS,
            Structures.ArrayStrategy.SUBSET
        );
        subsetMatcher2 = Structures.matcher(
            JsonDings.map(JSON),
            Structures.MAPS,
            Structures.ArrayStrategy.SUBSET
        );
        exactMatcher1 = Structures.matcher(
            JsonDings.json(JSON),
            Structures.MAPS,
            Structures.ArrayStrategy.EXACT
        );
        exactMatcher2 = Structures.matcher(
            JsonDings.map(JSON),
            Structures.MAPS,
            Structures.ArrayStrategy.EXACT
        );
    }

    @AfterEach
    void tearDown() {
        subsetMatcher1 = null;
        exactMatcher1 = null;
    }

    @Test
    void simpleSubsetIsPart() {
        assertPart(
            subsetMatcher1, JsonDings.json("""
                {
                  "foo":
                  {
                    "bar": 4
                  }
                }
                """)
        );
    }

    @Test
    void simpleDeviatingSubsetIsNotPart() {
        assertNotPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "foo": {
                    "bar": 5
                  }
                }
                """)
        );
    }

    @Test
    void explicitNullIsNotPart() {
        assertNotPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "foo": {
                    "bar": null
                  }
                }
                """)
        );
    }

    @Test
    void notPartIfArrayIsDifferent() {
        assertNotPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "argh": [7, 8] }
                      ]
                    }
                  }
                }
                """)
        );
        assertNotPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                    }
                  },
                  "arr2": [ true, "ouch" ]
                }
                """)
        );
        assertNotPart(
            subsetMatcher2,
            JsonDings.map("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "argh": [7, 8] }
                      ]
                    }
                  }
                }
                """)
        );
        assertNotPart(
            subsetMatcher2,
            JsonDings.map("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                    }
                  },
                  "arr2": [ true, "ouch" ]
                }
                """)
        );
    }

    @Test
    void notPartIfArrayIsActuallyObject() {
        assertNotPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "argh": { "foo": 5, "bar": 6 }}
                      ]
                    }
                  }
                }
                """)
        );
        assertNotPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "arr2": { "itsATrick": true, "reaction": "dip" }
                }
                """)
        );
        assertNotPart(
            subsetMatcher2,
            JsonDings.map("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "argh": { "foo": 5, "bar": 6 }}
                      ]
                    }
                  }
                }
                """)
        );
        assertNotPart(
            subsetMatcher2,
            JsonDings.map("""
                {
                  "arr2": { "itsATrick": true, "reaction": "dip" }
                }
                """)
        );
    }

    @Test
    void notPartIfFieldsDontMatchObject() {
        assertNotPart(
            """
                  {
                  "arr": [
                    {
                    "foo": 1,
                    "bar": 2
                    },
                    {
                    "foo": 3,
                    "bar": 4
                    }
                  ]
                }""",
            """
                {
                  "arr": [
                  {
                    "foo": 1,
                    "bar": 4
                  }
                  ]
                }
                """
        );
    }

    @Test
    void notPartUnlessStructureIsSame() {
        assertNotPart(
            """
                {
                  "foo": {
                    "bar": 5,
                    "zot": false
                  },
                  "bar": {
                     "bar": 6,
                     "zot": true
                  }
                }
                """,
            """
                {
                  "foo": {
                    "bar": 6,
                    "zot": false
                  }
                }
                """
        );
    }

    @Test
    void isPartIfArrayIsSubset() {
        assertPart(
            subsetMatcher1, JsonDings.json("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "rarg": [3]}
                      ]
                    }
                  }
                }
                """)
        );
    }

    @Test
    void isPartIfArrayIsExactMatch() {
        assertPart(
            exactMatcher1, JsonDings.json("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "argh": [ 4, 5, 6 ]},
                        { "rarg": [ 3, 2, 1 ]}
                      ]
                    }
                  }
                }
                """)
        );
    }

    @Test
    void isNotPartIfArrayIsNotExactMatch() {
        assertArrays(Structures.matcher(
            JsonDings.json(
                """
                    {
                      "foo": [ 1, 2, 3 ]
                    }
                    """),
            Structures.MAPS,
            Structures.ArrayStrategy.EXACT
        ));
        assertArraysMap(Structures.matcher(
            JsonDings.map(
                """
                    {
                      "foo": [ 1, 2, 3 ]
                    }
                    """),
            Structures.MAPS,
            Structures.ArrayStrategy.EXACT
        ));
    }

    @Test
    void isNotPartIfArrayIsNotSubsequence() {
        StructureMatcher<Object> matcher = Structures.matcher(
            JsonDings.json(
                """
                    {
                      "foo": [ 1, 2, 3, 4, 5 ]
                    }
                    """),
            Structures.MAPS,
            Structures.ArrayStrategy.SUBSEQ
        );
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 0, 1, 2, 3 ] }
                """)
        );
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 1, 3, 2 ] }
                """)
        );
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 1, 2, 4 ] }
                """)
        );
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 1, 2, 3, 5 ] }
                """)
        );
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 0, 1, 2, 3, 4 ] }
                """)
        );
        List.of(
                "1",
                "1, 2",
                "1, 2, 3",
                "1, 2, 3, 4, 5",
                "2, 3, 4, 5",
                "2, 3, 4",
                "2, 3",
                "2",
                "3, 4, 5",
                "3, 4",
                "3",
                "4",
                "4, 5",
                "5"
            )
            .forEach(part ->
                assertPart(
                    matcher, JsonDings.json("{ \"foo\": [" + part + "]}")
                ));
        assertPart(
            matcher, JsonDings.json("""
                { "foo": [ 1, 2, 3 ] }
                """)
        );
    }

    @Test
    void isPartIfArrayIsSubsetRegardlessOfOrder() {
        assertPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "argh": [6, 5]}
                      ]
                    }
                  }
                }
                """)
        );
    }

    @Test
    void isPartIfArrayIsSubsetRegardlessOfArity() {
        assertPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "argh": [6, 5, 5, 5, 5]}
                      ]
                    }
                  }
                }
                """)
        );
    }

    @Test
    void notPartIfArrayHasAdditionalElements() {
        assertNotPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "argh": [3, 4, 5, 6]}
                      ]
                    }
                  }
                }
                """)
        );
    }

    @Test
    void notPartIfArrayHasExplicityNulls() {
        assertPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "argh": [4]}
                      ]
                    }
                  }
                }
                """)
        );
        assertNotPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "foo": {
                    "bar": 4,
                    "zot": {
                      "zips": [
                        { "argh": [null]}
                      ]
                    }
                  }
                }
                """)
        );
    }

    @Test
    void deviatingPathsThroughListsAreReturned() {
        assertPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "departments": [
                    {
                      "tech": {
                        "employees": [
                           { "name": "Harry" }
                        ]
                      }
                    }
                  ]
                }
                """)
        );
        assertNotPart(
            subsetMatcher1,
            JsonDings.json("""
                {
                  "departments": [
                    {
                      "sales": {
                        "employees": [
                           { "name": "Harry" }
                        ]
                      }
                    }
                  ]
                }
                """)
        );
    }

    @Test
    void arrayTest() {
        assertNotPart(subsetMatcher1, JsonDings.json("[3]"));
    }

    private static final String JSON =
        """
            {
              "foo": {
                "bar": 4,
                "zot": {
                  "zip": 45.4,
                  "zips": [
                   { "argh": [ 4, 5, 6 ]},
                   { "rarg": [ 3, 2, 1 ]}
                  ]
                }
              },
              "arr2": [ "dip", 5, true ],
              "departments": [
                {
                    "tech": {
                      "employees": [
                         { "name": "Harry", "salary": 4.5 },
                         { "name": "Sally", "salary": 5.5 }
                      ]
                  }
                },
                {
                    "sales": {
                      "employees": [
                        { "name": "Dumb", "salary": 10.5 },
                        { "name": "Dumber", "salary": 11.5 }
                      ]
                    }
                }
              ]
            }
            """;

    private static void assertArrays(StructureMatcher<Object> matcher) {
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 0, 1, 2, 3 ] }
                """)
        );
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 1, 3, 2 ] }
                """)
        );
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 1, 2, 4 ] }
                """)
        );
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 1, 2, 3, 4 ] }
                """)
        );
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 0, 1, 2, 3, 4 ] }
                """)
        );
        assertPart(
            matcher, JsonDings.json("""
                { "foo": [ 1, 2, 3 ] }
                """)
        );
        assertNotPart(
            matcher,
            JsonDings.json("""
                { "foo": [ 1, 2 ] }
                """)
        );
    }

    private static void assertArraysMap(StructureMatcher<Object> matcher) {
        assertNotPart(matcher, JsonDings.map(
            """
                { "foo": [ 0, 1, 2, 3 ] }
                """)
        );
        assertNotPart(matcher, JsonDings.map(
            """
                { "foo": [ 1, 3, 2 ] }
                """)
        );
        assertNotPart(matcher, JsonDings.map(
            """
                { "foo": [ 1, 2, 4 ] }
                """)
        );
        assertNotPart(matcher, JsonDings.map(
            """
                { "foo": [ 1, 2, 3, 4 ] }
                """)
        );
        assertNotPart(matcher, JsonDings.map(
            """
                { "foo": [ 0, 1, 2, 3, 4 ] }
                """)
        );
        assertPart(matcher, JsonDings.map(
            """
                { "foo": [ 1, 2, 3 ] }
                """)
        );
        assertNotPart(matcher, JsonDings.map(
            """
                { "foo": [ 1, 2 ] }
                """)
        );
    }

    private static <T> void assertNotPart(StructureMatcher<T> matcher, T json) {
        assertThat(matcher.contains(json))
            .describedAs("Should not be part of: " + matcher + "\n subset : " + JsonDings.write(json))
            .isFalse();
    }

    private static <T> void assertPart(StructureMatcher<T> matcher, T json) {
        assertThat(matcher.contains(json))
            .describedAs("Should be a part: " + JsonDings.write(json))
            .isTrue();
    }

    private static void assertNotPart(String json, String subset) {
        assertNotPart(
            Structures.matcher(
                JsonDings.json(json),
                Structures.MAPS,
                Structures.ArrayStrategy.SUBSET
            ),
            JsonDings.json(subset)
        );
        assertNotPart(
            Structures.matcher(
                JsonDings.map(json),
                Structures.MAPS,
                Structures.ArrayStrategy.SUBSET
            ),
            JsonDings.map(subset)
        );
    }
}
