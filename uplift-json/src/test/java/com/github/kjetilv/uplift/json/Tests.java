package com.github.kjetilv.uplift.json;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.assertj.core.api.InstanceOfAssertFactories;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class Tests {

    static void testObjects(Json instance) throws MalformedURLException {
        Map<String, Object> written = new LinkedHashMap<>();
        written.put("uri", Optional.of(URI.create("https://www.vg.no")));
        written.put("url", URI.create("https://www.vg.no").toURL());
        written.put("whatever", Enum.ENUM);
        written.put("oops", null);
        Map<?, ?> parsed = instance.jsonMap(
            """
            { "uri": "https://www.vg.no", "url": "https://www.vg.no", "whatever": "ENUM", "oops": null }
            """);
        String expected = instance.write(parsed);
        String actual = instance.write(written);
        assertEquals(actual, expected);
    }

    static void testRestAi(Json instance) {
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
                  """,
            instance
        );
    }

    static void testAws(Json instance) {
        //language=JSON
        Object roundtrip = roundtrip(
            """
            {
              "foo": {},
              "bar": {}
            }
            """
            , instance);
        org.assertj.core.api.Assertions.assertThat(roundtrip)
            .asInstanceOf(InstanceOfAssertFactories.MAP)
            .hasSize(2);
    }

    static void testEscaped(Json instance) {
        roundtrip(
            """
            {
            "foo": "\\{"
            }
            """,
            instance
        );
    }

    static void test(Json instance) {
        roundtrip("""
                  {
                    "foo": {

                      "bar":    5,

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
                      null,
                      "dette er en test"
                    ]          ,
                    "nothing"  : null
                  }
                  """, instance);
    }

    static void terminateString(Json instance) {
        failedRead(
            """
            {
              true: 5,
              "bar": "stuff
              "
            }
            """,
            e ->
                assertReadException(e, "\"stuff", 3, 15), instance
        );
    }

    static void noStuff(Json instance) {
        failedRead(
            """
            {
              true: 5,
              "bar": stuff
            }
            """,
            e ->
                assertReadException(e, "s", 3, 10), instance
        );
    }

    static void noSemis(Json instance) {
        failedRead(
            """
            {
              true: 5;
              "bar": true
            }
            """,
            e ->
                assertReadException(e, ";", 2, 10), instance
        );
    }

    static void failedParse(
        String source,
        Consumer<? super ParseException> failer
    ) {
        try {
            fail("Unexpected success: " + Json.INSTANCE.read(source));
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

    static void emptyStuff(Json instance) {
        roundtrip(
            """
            {}
            """,
            instance
        );
    }

    static void emptyString(Json instance) {
        assertThat(roundtrip(
            """
            { "foo": "" }
            """,
            instance
        )).asInstanceOf(InstanceOfAssertFactories.MAP).containsEntry("foo", "");
    }

    static void assertParseException(ParseException e, TokenType unexpected, int line, TokenType... expected) {
        Collection<TokenType> set = EnumSet.copyOf(Arrays.asList(expected));
        assertEquals(line, e.getToken().line());
        assertEquals(unexpected, e.getToken().type());
        assertEquals(set.size(), e.getExpected().size(),
            () -> set + " /= " + e.getExpected());
        assertTrue(e.getExpected().containsAll(set));
    }

    enum Enum {

        ENUM
    }

    private Tests() {

    }

    private static void failedRead(
        String source,
        Consumer<? super ReadException> failer, Json instance
    ) {
        try {
            fail("Unexpected success: " + instance.read(
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

    private static void assertReadException(ReadException e, String lexeme, int line, int column) {
        assertEquals(lexeme, e.getLexeme());
        assertEquals(line, e.getLine());
        assertEquals(column, e.getColumn());
    }

    private static Object roundtrip(String source, Json instance) {
        Object json = instance.read(source);
        String source2 = instance.write(json);
        byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
        Object json2 =
            instance.read(new ByteArrayInputStream(bytes));
        assertEquals(json, instance.read(source2));
        assertEquals(json2, instance.read(source2));
        return json;
    }
}
