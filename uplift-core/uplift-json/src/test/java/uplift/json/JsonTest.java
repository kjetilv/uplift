package uplift.json;

import java.net.MalformedURLException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static uplift.json.TokenType.BOOL;
import static uplift.json.TokenType.COMMA;
import static uplift.json.TokenType.END_ARRAY;
import static uplift.json.TokenType.END_OBJECT;
import static uplift.json.TokenType.STRING;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class JsonTest {

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
                Tests.assertParseException(e, BOOL, 2, STRING)
        );
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
                Tests.assertParseException(e, STRING, 3, COMMA, END_OBJECT)
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
                Tests.assertParseException(e, BOOL, 3, COMMA, END_ARRAY)
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
    void failOnObjects() {
        try {
            fail("Should not accept " + Json.INSTANCE.write(Stream.empty()));
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Bad object"));
        }
    }
}
