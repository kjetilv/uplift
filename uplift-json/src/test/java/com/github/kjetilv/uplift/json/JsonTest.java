package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.tokens.ReadException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.json.TokenType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class JsonTest {

    static void failedParse(
        String source,
        Consumer<? super ParseException> failer
    ) {
        try {
            fail("Unexpected success: " + JSON.read(source));
        } catch (ParseException e) {
            failer.accept(e);
        } catch (Exception e) {
            if (e.getCause() instanceof ParseException parseException) {
                failer.accept(parseException);
            } else {
                fail("Unexpected exception", e);
            }
        }
    }

    static void assertFailedParse(
        String source,
        Consumer<? super AssertionError> failer
    ) {
        try {
            fail("Unexpected success: " + JSON.read(source));
        } catch (AssertionError e) {
            failer.accept(e);
        } catch (Exception e) {
            fail("Unexpected exception", e);
        }
    }

    static void assertParseException(ParseException e, TokenType unexpected, TokenType... expected) {
        List<TokenType> set = Arrays.asList(expected);
        assertEquals(unexpected, e.getToken().tokenType());
        assertEquals(
            set.size(), e.getExpected().size(),
            () -> set + " /= " + e.getExpected()
        );
        assertThat(e.getExpected()).containsAll(set);
    }

    @Test
    void singleValueTrue() {
        //language=json
        Object read = JSON.read("true");
        assertThat(read).isEqualTo(true);
    }

    @Test
    void singleValueString() {
        //language=json
        Object read = JSON.read("\"foo\"");
        assertThat(read).isEqualTo("foo");
    }

    @Test
    void singleValueStringmoji() {
        //language=json
        Object read = JSON.read("\"🃑\"");
        assertThat(read).isEqualTo("🃑");
    }

    @Test
    void singleValueDec() {
        Object read = JSON.read("0.42");
        assertThat(read).isEqualTo(new BigDecimal("0.42"));
    }

    @Test
    void numberValue() {
        Object read = JSON.read("""
            { "foo": 0.42 }
            """);
        assertThat(read).asInstanceOf(MAP).containsEntry("foo", new BigDecimal("0.42"));
    }

    @Test
    void emptyArray() {
        //language=json
        Object read1 = JSON.read("[]");
        assertThat(read1).asInstanceOf(LIST).isEmpty();
        //language=json
        Object read2 = JSON.read(
            """
                { "foo": [] }
                """);
        assertThat(read2).asInstanceOf(InstanceOfAssertFactories.MAP)
            .hasEntrySatisfying(
                "foo", entry ->
                    assertThat(entry).asInstanceOf(LIST).isEmpty()
            );
    }

    @Test
    void noSemis() {
        failedRead(
            """
                {
                  "foo": 5;
                  "bar": true
                }
                """,
            e ->
                assertReadException(e, ";")
        );
    }

    @Test
    void noStuff() {
        failedRead(
            """
                {
                  "true": 5,
                  "bar": stuff
                }
                """,
            e ->
                assertReadException(e, "s")
        );
    }

    @Test
    void emptyStuff() {
        //language=json
        roundtrip(
            """
                {}
                """
        );
    }

    @Test
    void emptyString() {
        assertThat(roundtrip(
            //language=json
            """
                { "foo": "" }
                """
        )).asInstanceOf(InstanceOfAssertFactories.MAP).containsEntry("foo", "");
    }

    @Test
    void terminateString() {
        failedRead(
            """
                {
                  "bar": "stuff
                  "
                }
                """,
            e ->
                assertReadException(e, "stuff")
        );
    }

    @Test
    void requireStringField() {
        failedParse(
            """
                {
                  true: 5
                  "bar": true
                }
                """,
            e ->
                assertParseException(e, BOOL, STRING, END_OBJECT)
        );
    }

    @Test
    void negationsOnlyPrepended() {
        failedParse(
            """
                {
                  "bar": 5-5
                }
                """,
            e ->
                assertParseException(e, NUMBER, COMMA, END_OBJECT)
        );
    }

    @Test
    void malformednumbers1() {
        assertThatThrownBy(() -> JSON.read("""
            {
              "bar": .-
            }
            """)).
            satisfies(e ->
                assertThat(e.toString()).contains("Failed to parse: `.`")
            );
    }

    @Test
    void malformednumbers2() {
        assertThatThrownBy(() -> JSON.read("""
            {
              "bar": -
            }
            """))
            .satisfies(e ->
                assertThat(e.toString()).contains("Failed to parse: `-`")
            );
    }

    @Test
    void malformednumbers3() {
        try {
            fail("Should not scan: " + JSON.read("""
                {
                  "bar": 0-.
                }
                """));
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("Failed to parse: `-.`");
        }
    }

    @Test
    void malformednumbers4() {
        assertThatThrownBy(() ->
            JSON.read("""
                {
                  "bar": -.
                }
                """))
            .satisfies(e ->
                assertThat(e.toString()).contains("Failed to parse: `-.`"));
    }

    @Test
    void malformednumbers5() {
        assertThatThrownBy(() ->
            JSON.read("""
                {
                  "bar": .-
                }
                """))
            .satisfies(e ->
                assertThat(e.toString()).contains("Failed to parse: `.`"));
    }

    @Test
    void malformednumbers6() {
        try {
            fail("Should not scan: " + JSON.read("""
                {
                  "bar": 0.-
                }
                """));
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("Failed to parse: `-`");
        }
    }

    @Test
    void requireCommaInObject() {
        failedParse(
            """
                {
                  "foo": 5
                  "bar": true
                }
                """,
            e ->
                assertParseException(e, STRING, COMMA, END_OBJECT)
        );
    }

    @Test
    void requireCommaInArray() {
        failedParse(
            """
                {
                  "foo": 5,
                  "bar": [true, 5 false]
                }
                """,
            e ->
                assertParseException(e, BOOL, COMMA, END_ARRAY)
        );
    }

    @Test
    void requireCommaInArrayAfterArray() {
        failedParse(
            """
                {
                  "foo": 5,
                  "bar": [true, 5, [3] false]
                }
                """,
            e -> {
            }
        );
    }

    @Test
    void requireCommaInArrayAfterObject() {
        failedParse(
            """
                {
                  "foo": 5,
                  "bar": [true, 5, {} false]
                }
                """,
            e ->
                assertParseException(e, BOOL, COMMA, END_ARRAY)
        );
    }

    @Test
    void literalTrue() {
        assertFailedParse(
            """
                {
                  "foo": 5,
                  "bar": [troo]
                }
                """,
            e ->
                assertThat(e).isInstanceOf(AssertionError.class)
        );
    }

    @Test
    void quotedString() {
        roundtrip(
            """
                {
                  "foo": "\\"bar\\""
                }
                """
        );
    }

    @Test
    void literalTrueToo() {
        assertFailedParse(
            """
                tru
                """,
            e ->
                assertThat(e).isInstanceOf(AssertionError.class)
        );
    }

    @Test
    void literalFalse() {
        assertFailedParse(
            """
                falsy
                """,
            e ->
                assertThat(e).isInstanceOf(AssertionError.class)
        );
    }

    @Test
    void literalNulll() {
        assertFailedParse(
            """
                nil
                """,
            e ->
                assertThat(e).isInstanceOf(AssertionError.class)
        );
    }

    @Test
    void test() {
        //language=json
        roundtrip("""
            {
              "foo": {
            
                "bar":    5,
                "arr2": [2, 3],
            
                "halfdig": 3.0,
                "dighalf": 0.3,
                "flote":   -3.14,
                "streng" :   "dette er en test",
                "problemStreng": "\\"foo\\""
              },
              "zot": [
                421,
                true,
                { "another": ["object", "for",    17 ], "zot":     "zip" },
                [ "foo" ],
                null,
                "dette er en test"
              ]          ,
              "nothing"  : null
            }
            """
        );
    }

    @Test
    void testEscaped() {
        //language=json
        roundtrip(
            """
                {
                  "foo": "\\{"
                }
                """
        );
    }

    @Test
    void testAws() {
        //language=JSON
        Object roundtrip = roundtrip(
            """
                {
                  "foo": {},
                  "bar": {}
                }
                """
        );
        assertThat(roundtrip)
            .asInstanceOf(InstanceOfAssertFactories.MAP)
            .hasSize(2);
    }

    @Disabled("Not yet")
    @Test
    void testUnidcode() {
        //language=JSON
        Object roundtrip = roundtrip(
            """
                {
                  "♥︎": "♣︎"
                }
                """
        );
        assertThat(roundtrip)
            .asInstanceOf(InstanceOfAssertFactories.MAP)
            .hasSize(2);
    }

    @Test
    void test48jso() {
        //language=JSON
        Object roundtrip = roundtrip(
            """
                {
                  "in_reply_to_status_id":                 null,
                  "text": "@firawidya iya fir,hihi. Rencananya awal bulan mau kesana. Ada model br gak?",
                  "activities": {
                    "retweeters_count": "0",
                    "retweeters": [],
                    "favoriters": [],
                    "favoriters_count": "0",
                    "repliers": [],
                    "repliers_count": "0"
                  }
                }
                """
        );
        assertThat(roundtrip)
            .asInstanceOf(InstanceOfAssertFactories.MAP)
            .hasSize(3);
    }

    @Test
    void testNills() {
        //language=JSON
        Object roundtrip = roundtrip(
            """
                { "bar": [{
                  "foofoofoofoofoofoofoofoofoofoofoofoofoofoofoo": null,
                  "bar": false,
                  "zot": true,
                  "ans": 42
                  }, null
                ]
                }
                """
        );
        assertThat(roundtrip)
            .asInstanceOf(InstanceOfAssertFactories.MAP)
            .hasSize(1)
            .hasEntrySatisfying(
                "bar", v ->
                    assertThat(v).asInstanceOf(LIST)
                        .hasSize(2)
                        .first()
                        .asInstanceOf(MAP)
                        .hasSize(4)
                        .containsEntry("bar", false)
            );
    }

    @Test
    void testRestAi() {
        //language=json
        roundtrip(
            """
                {
                   "resource": "/lease",
                   "path": "/lease",
                   "httpMethod": "POST",
                   "requestContext": {
                     "resourceId": "witwcb",
                     "resourcePath": "/lease",
                     "httpMethod": "POST",
                     "extendedRequestId": "ebrKOGuugi0Fvnw=",
                     "requestTime": "08/Jan/2023:16:41:37 +0000",
                     "path": "/lease",
                     "accountId": "732946774009",
                     "protocol": "HTTP/1.1",
                     "stage": "test-invoke-stage",
                     "domainPrefix": "testPrefix",
                     "requestTimeEpoch": 1673196097248,
                     "requestId": "2ceba4e4-1117-41d6-8368-95fcbc89fc66",
                     "identity": {
                       "apiKey": "test-invoke-api-key",
                       "userArn": "arn:aws:iam::732946774009:root",
                       "apiKeyId": "test-invoke-api-key-id",
                       "userAgent": "aws-internal/3 aws-sdk-java/1.12.358 Linux/5.4.225-139.416.amzn2int.x86_64 OpenJDK_64-Bit_Server_VM/25.352-b10 java/1.8.0_352 vendor/Oracle_Corporation cfg/retry-mode/standard",
                       "accountId": "732946774009",
                       "caller": "732946774009",
                       "sourceIp": "test-invoke-source-ip",
                       "accessKey": "ASIA2VJYIG74WUKMZ2K3",
                       "user": "732946774009"
                     },
                     "domainName": "testPrefix.testDomainName",
                     "apiId": "a3c0yfwlt2"
                   },
                   "body": "\\n{\\"accessToken\\":\\"EAAFqWL02SZBgBAB1GI3kuZBg2bOx05gmeo3vZCVbQtrHdZCyZC8pzlPuZAfPHseH1d77ZBvGNrNQl87Qz1KYDn0PZBeI6IHvGeWgzH8WHWm49LHUPenhIitU14yL1LZBa7uwzfxtRQjGueki4Vi44HLrHz5khNRhXNlrIBXQ9OVv2i7zBEPGZCU6BElVkoIOa6F0tUiai2zHVEBMwistgHvDAspZBqdmoGnPIkZD\\",\\"userID\\":\\"2787973921215833\\",\\"expiresIn\\":4724,\\"signedRequest\\":\\"SEGUyFsnAoJNi4VesQzEqB-wYmfuJNusT7UTKMVXGnA.eyJ1c2VyX2lkIjoiMjc4Nzk3MzkyMTIxNTgzMyIsImNvZGUiOiJBUUI0aUpTMmYzTTZfRkRFbFJWcEQ4dnA1eXVGeWVvVmIzWk5OM0JveW0tYmpHazJiMGxIZ0VGS1ZNVmhHTWV2ZXluYWtwamk2c0p4cV9QNlp4REpvMjZhdGY4WVNQSEJ0YmRTOG1QdzN0Vy1yMVpwYlBkam0zNXhWRnV3bmxXSS1YLXB0VlI3SnNnUTRQWUF3MUx0V19qV2dBT3hpcFRkalhZcFlwbHhXX1V3UkFJQTVPNzBTcDNiNm52YXlMTk8teW0tVHJnUWpYN0NuS3VNSkVzNEVESjVpWnJDY0RJYkhxczRBc2JWTUg1RWZvNWZsTWIxMGtXaDdGTHZrSFhDSjI2cHY2UVM5Rld2dW5fUHB1Rk5JbDFxX05zSkFRb1N3S2Ywek5Qc2U0V0RCaHJUYklOcHNLWXU5MEs3cy1vNjNzNjg0OGQ0WTVhVGVhVmZRSGJvLTRSQ3VIS2lZTGdxblpnQk5nNnpGa18xZlEiLCJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImlzc3VlZF9hdCI6MTY3MzE5NjA3Nn0\\",\\"graphDomain\\":\\"facebook\\",\\"data_access_expiration_time\\":1680972076}",
                   "isBase64Encoded": false
                }
                """
        );
    }

    @Test
    void testObjects() throws MalformedURLException {
        Map<String, Object> written = new LinkedHashMap<>();
        written.put("uri", Optional.of(URI.create("https://www.vg.no")));
        written.put("url", URI.create("https://www.vg.no").toURL());
        written.put("whatever", Enum.ENUM);
        written.put("oops", null);
        //language=json
        String source = """
            {"uri":"https://www.vg.no","url":"https://www.vg.no","whatever":"ENUM","oops":null}
            """.trim();
        Map<?, ?> parsed = JSON.jsonMap(source);
        String expected = JSON.write(parsed);
        assertThat(expected).isEqualTo(source);
        String actual = JSON.write(written);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Disabled
    void failOnObjects() {
        try {
            fail("Should not accept " + JSON.write(Stream.empty()));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Bad object");
        }
    }

    public static final Json JSON = Json.INSTANCE;

    private static void failedRead(
        String source,
        Consumer<? super ReadException> failer
    ) {
        try {
            fail("Unexpected success: " + JSON.read(
                source
            ));
        } catch (ReadException e) {
            failer.accept(e);
        } catch (Exception e) {
            if (e.getCause() instanceof ReadException readException) {
                failer.accept(readException);
            } else {
                fail("Unexpected exception: " + e);
            }
        }
    }

    private static void assertReadException(ReadException e, String lexeme) {
        assertThat(e.toString()).contains("`" + lexeme + "`");
    }

    private static Object roundtrip(
        //language=json
        String source
    ) {
        Object json = JSON.read(source);
        String source2 = JSON.write(json);
        byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
        Object json2 =
            JSON.read(new ByteArrayInputStream(bytes));
        assertEquals(json, JSON.read(source2));
        assertEquals(json2, JSON.read(source2));
        return json;
    }

    enum Enum {

        ENUM
    }
}
