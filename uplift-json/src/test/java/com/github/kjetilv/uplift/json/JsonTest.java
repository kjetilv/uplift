package com.github.kjetilv.uplift.json;

import java.net.MalformedURLException;
import java.nio.channels.ScatteringByteChannel;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.github.kjetilv.uplift.json.Tests.assertParseException;
import static com.github.kjetilv.uplift.json.TokenType.BOOL;
import static com.github.kjetilv.uplift.json.TokenType.COMMA;
import static com.github.kjetilv.uplift.json.TokenType.END_ARRAY;
import static com.github.kjetilv.uplift.json.TokenType.END_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.TokenType.STRING;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class JsonTest {

    @Test
    void emprtyArray() {
        Tests.emptyArray(Json.INSTANCE);
    }

    @Test
    void noSemis() {
        Tests.noSemis(Json.INSTANCE);
    }

    @Test
    void noStuff() {
        Tests.noStuff(Json.INSTANCE);
    }

    @Test
    void emptyStuff() {
        Tests.emptyStuff(Json.INSTANCE);
    }

    @Test
    void emptyString() {
        Tests.emptyString(Json.INSTANCE);
    }

    @Test
    void terminateString() {
        Tests.terminateString(Json.INSTANCE);
    }

    @Test
    void requireStringField() {
        Tests.failedParse(
            """
            {
              true: 5
              "bar": true
            }
            """,
            e ->
                assertParseException(e, BOOL, 2, STRING)
        );
    }

    @Test
    void negationsOnlyPrepended() {
        Tests.failedParse(
            """
            {
              "bar": 5-5
            }
            """,
            e ->
                assertParseException(e, NUMBER, 2, COMMA, END_OBJECT)
        );
    }

    @Test
    void malformednumbers1() {
        try {
            fail("Should not scan: " + Json.INSTANCE.read("""
            {
              "bar": .-
            }
            """));
       } catch (Exception e) {
            assertTrue(e.getMessage().contains("Failed to scan"));
        }
    }

    @Test
    void malformednumbers2() {
        try {
            fail("Should not scan: " + Json.INSTANCE.read("""
            {
              "bar": -
            }
            """));
       } catch (Exception e) {
            assertTrue(e.getMessage().contains("Failed to scan"));
        }
    }

    @Test
    void malformednumbers3() {
        try {
            fail("Should not scan: " + Json.INSTANCE.read("""
            {
              "bar": 0-.
            }
            """));
       } catch (Exception e) {
            assertTrue(e.getMessage().contains("Failed to scan"));
        }
    }

    @Test
    void malformednumbers4() {
        try {
            fail("Should not scan: " + Json.INSTANCE.read("""
            {
              "bar": -.
            }
            """));
       } catch (Exception e) {
            assertTrue(e.getMessage().contains("Failed to scan"));
        }
    }

    @Test
    void malformednumbers5() {
        try {
            fail("Should not scan: " + Json.INSTANCE.read("""
            {
              "bar": .-
            }
            """));
       } catch (Exception e) {
            assertTrue(e.getMessage().contains("Failed to scan"));
        }
    }

    @Test
    void malformednumbers6() {
        try {
            fail("Should not scan: " + Json.INSTANCE.read("""
            {
              "bar": 0.-
            }
            """));
       } catch (Exception e) {
            assertTrue(e.getMessage().contains("Failed to scan"));
        }
    }

    @Test
    void requireCommaInObject() {
        Tests.failedParse(
            """
            {
              "foo": 5
              "bar": true
            }
            """,
            e ->
                assertParseException(e, STRING, 3, COMMA, END_OBJECT)
        );
    }

    @Test
    void requireCommaInArray() {
        Tests.failedParse(
            """
            {
              "foo": 5,
              "bar": [true, 5 false]
            }
            """,
            e ->
                assertParseException(e, BOOL, 3, COMMA, END_ARRAY)
        );
    }

    @Test
    void test() {
        Tests.test(Json.INSTANCE);
    }

    @Test
    void testEscaped() {
        Tests.testEscaped(Json.INSTANCE);
    }

    @Test
    void testAws() {
        Tests.testAws(Json.INSTANCE);
    }

    @Test
    void testRestAi() {
        Tests.testRestAi(Json.INSTANCE);
    }

    @Test
    void testObjects() throws MalformedURLException {
        Tests.testObjects(Json.INSTANCE);
    }

    @Test
    @Disabled
    void failOnObjects() {
        try {
            fail("Should not accept " + Json.INSTANCE.write(Stream.empty()));
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Bad object"));
        }
    }
}
