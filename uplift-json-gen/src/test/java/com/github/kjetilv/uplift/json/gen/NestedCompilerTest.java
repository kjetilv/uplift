package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
class NestedCompilerTest extends CompilerTestCase {

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
    void nestedTypeArray() {
        ver(//language=java
            """
                public record Eagle(Nest[] nests) {
                
                    record Nest(String foo) {}
                
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof Eagle(var onests) && Arrays.equals(nests, onests);
                    }
                
                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(nests);
                    }
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

    @Test
    void compelexNestedTypeArray() {
        ver(//language=java
            """
                public record Eagle(List<Nest> nests) {
                    record Nest(String foo, int[] nums) {
                
                        @Override
                        public boolean equals(Object obj) {
                            return obj instanceof Nest(var ofoo, var onums) &&
                                Objects.equals(foo, ofoo) &&
                                Arrays.equals(nums, onums);
                        }
                
                        @Override
                        public int hashCode() {
                           return Objects.hash(foo, Arrays.hashCode(nums));
                        }
                    }
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
