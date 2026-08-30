package com.github.kjetilv.uplift.json.gen;

import module java.base;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ClassNameDiffersFromFileName")
public class ArraysCompilerTest extends CompilerTestCase {

    @Test
    void stringArrayFields() {
        ver(//language=java
            """
                public record SingleArrayField(String[] ss) {
                
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SingleArrayField(var oss) && Arrays.equals(ss, oss);
                    }
                
                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(ss);
                    }
                }
                """,
            //language=json
            """
                {
                  "ss": ["foo"]
                }
                """,
            //language=json
            """
                {
                  "ss": [""]
                }
                """,
            //language=json
            """
                {
                  "s": []
                }
                """,
            //language=json
            """
                {
                  "s": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void longPrimitiveArrayFields() {
        ver(//language=java
            """
                public record SingleField(long[] ls) {
                
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SingleField(var ols) && Arrays.equals(ls, ols);
                    }
                
                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(ls);
                    }
                }
                """,
            //language=json
            """
                {
                  "ls": [1000]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }

    @Test
    void longArrayFields() {
        ver(
            //language=java
            """
                public record SingleField(Long[] ls) {
                
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SingleField(var ols) && Arrays.equals(ls, ols);
                    }
                
                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(ls);
                    }
                }
                """,
            //language=json
            """
                {
                  "ls": [ 42 ]
                }
                """,
            //language=json
            """
                {
                  "ls": [ 42, 1729 ]
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {}
                """
        );
    }
    @Test
    void floatPrimitiveArrayFields() {
        ver(//language=java
            """
                public record SingleField(float[] fs) {
                
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SingleField(var ofs) && Arrays.equals(fs, ofs);
                    }
                
                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(fs);
                    }
                }
                """,
            //language=json
            """
                {
                  "ls": [1000.0]
                }
                """,
            //language=json
            """
                {
                  "ls": []
                }
                """,
            //language=json
            """
                {
                  "ls": null
                }
                """,
            //language=json
            "{}"
        );
    }
    @Test
    void booleanArrayFields() {
        ver(//language=java
            """
                public record SingleField(boolean[] bs) {
                
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SingleField(var obs) && Arrays.equals(bs, obs);
                    }
                
                    @Override
                    public int hashCode() {
                       return Arrays.hashCode(bs);
                    }
                }
                """,
            //language=json
            """
                {
                  "bs": [false, false, true]
                }
                """,
            //language=json
            """
                {
                  "bs": []
                }
                """,
            //language=json
            """
                {
                  "bs": null
                }
                """,
            //language=json
            "{}"
        );
    }

}
