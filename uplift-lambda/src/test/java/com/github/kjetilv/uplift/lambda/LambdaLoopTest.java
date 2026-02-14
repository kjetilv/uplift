package com.github.kjetilv.uplift.lambda;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("MagicNumber")
class LambdaLoopTest {

    @Test
    void flow() {
        Collection<String> responses = new ArrayList<>();
        var failed = new AtomicReference<Throwable>();
        var request = HttpRequest.newBuilder(URI.create("http://localhost")).GET().build();
        LambdaLoopers.looper(
            "test",
            () ->
                responses.size() > 3
                    ? Optional.empty()
                    : Optional.of(CompletableFuture.supplyAsync(() -> Invocation.create(
                            UUID.randomUUID().toString(),
                            request,
                            LambdaPayload.parse(REQ), Instant.now()
                        )
                    )),
            _ ->
                new LambdaResult(
                    200,
                    Collections.emptyMap(),
                    "OK".getBytes(StandardCharsets.UTF_8),
                    false
                ),
            invocation -> {
                var uri = invocation.request().uri();
                var resolve = uri.resolve("/bar/" + invocation.id() + "/foo");
                return HttpRequest.newBuilder(resolve).GET().build();
            },
            invocation -> {
                var ok = new String(invocation.result().body(), StandardCharsets.UTF_8);
                responses.add(ok);
                return invocation.completionFuture(
                    () -> CompletableFuture.completedStage(ok),
                    Instant::now
                );
            },
            (_, throwable) -> {
                failed.set(throwable);
                return throwable == null;
            },
            Instant::now
        ).run();
        assertThat(responses.size()).isGreaterThanOrEqualTo(3);
        assertThat(failed.get()).isNull();
    }

    private static final String REQ =
        """
            {
              "resource": "/lease",
              "path": "/lease",
              "httpMethod": "POST",
              "headers": {
                "Accept": "*/*",
                "Accept-Encoding": "gzip, deflate, br",
                "Accept-Language": "en-GB,en-US;q=0.9,en;q=0.8",
                "Content-Type": "application/json",
                "Host": "4fmbt35l1k.execute-api.eu-north-1.amazonaws.com",
                "Origin": "kjetilv.github.io",
                "User-Agent": "Apache-HttpClient/4.5.13 (Java/17.0.5)",
                "X-Amzn-Trace-Id": "Root=1-63bc2212-01f783dc75b6866c0bb986eb",
                "X-Forwarded-For": "45.84.39.33",
                "X-Forwarded-Port": "443",
                "X-Forwarded-Proto": "https"
              },
              "multiValueHeaders": {
                "Accept": [
                  "*/*"
                ],
                "Accept-Encoding": [
                  "gzip, deflate, br"
                ],
                "Accept-Language": [
                  "en-GB,en-US;q=0.9,en;q=0.8"
                ],
                "Content-Type": [
                  "application/json"
                ],
                "Host": [
                  "4fmbt35l1k.execute-api.eu-north-1.amazonaws.com"
                ],
                "Origin": [
                  "kjetilv.github.io"
                ],
                "User-Agent": [
                  "Apache-HttpClient/4.5.13 (Java/17.0.5)"
                ],
                "X-Amzn-Trace-Id": [
                  "Root=1-63bc2212-01f783dc75b6866c0bb986eb"
                ],
                "X-Forwarded-For": [
                  "45.84.39.33"
                ],
                "X-Forwarded-Port": [
                  "443"
                ],
                "X-Forwarded-Proto": [
                  "https"
                ]
              },
              "requestContext": {
                "resourceId": "h5ybv1",
                "resourcePath": "/lease",
                "httpMethod": "POST",
                "extendedRequestId": "eepC_E89Ai0FfDg=",
                "requestTime": "09/Jan/2023:14:17:54 +0000",
                "path": "/prod/lease",
                "accountId": "732946774009",
                "protocol": "HTTP/1.1",
                "stage": "prod",
                "domainPrefix": "4fmbt35l1k",
                "requestTimeEpoch": 1673273874959,
                "requestId": "cef185df-25f8-4149-a84b-a5af730c0092",
                "identity": {
                  "sourceIp": "45.84.39.33",
                  "userAgent": "Apache-HttpClient/4.5.13 (Java/17.0.5)"
                },
                "domainName": "4fmbt35l1k.execute-api.eu-north-1.amazonaws.com",
                "apiId": "4fmbt35l1k"
              },
              "body": "{\\"accessToken\\":\\"EAAFqWL02SZBgBAEDx1sfn3mVoWDePnYpSZAjaIY4bIHZC9W0D3ZBTlCyPZB6q9ugCSTmY2dXCLrfzudwmB9UcIusv5PgR0l4DLq2AZBofhZCZAhvTG2CtLFhNf316ZA1CNreryFERBIY6jNuRVRrRUgpzjHYCTvBJaVnm0WEBSIh8PzeJHaWoslGMUVNDqyZCQnyTX4bkcOW8hQZAUxZCCEOyPugHtez9ePwfTYZD\\",\\"userID\\":\\"2787973921215833\\",\\"expiresIn\\":4145,\\"signedRequest\\":\\"XFtpvJfEre0VCXfQo_26CieeIpMp6kCh-4vr7gEmRQc.eyJ1c2VyX2lkIjoiMjc4Nzk3MzkyMTIxNTgzMyIsImNvZGUiOiJBUUFlTll1SWRPOGY0eTFfZlZFd3hyRGFVcXgyNGFZbTVpeGlremhYVGNzOWJodFBIdjljVWM2VFNXa2JmSU9OR2E0dmtoRGRQVzQxSEJ6N2NyQW9Sak90aEppWV9DeV9rM0U3MFJ5N3Vsc2o2OGJ2dUhSOHpjcGE2N214SlkyMXlVU3NKei1NMlJ2ODNHU2N5YUIyanA5WXlNVmRaX1ZKbTRtMmNCSjFaLTVGNDhLWWc1NzJ5cTlVR1VDU3ZnVU5mRDk3dDl5bUcycjlDYUY1b3BuU015WE9TMktQSmlscDVoTENzcEh6WXZhYURPU1hxZ180NDd3YjV0R3UtQUpaOXhOS0JFTG1XcWJPaWhYQ3RBZEExZ0FMbkljMFVkUkVKUGVoQTk2LTRhR2dSRERibEhndlVsSFF4WjJoa2kyeWdKVkI2RjJIV0ROTmZURkVyNk45dXZlV3VOT3BwTGR6cXdic3M3dG9xNElzVVEiLCJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImlzc3VlZF9hdCI6MTY3MzI3MjI1NX0\\",\\"graphDomain\\":\\"facebook\\",\\"data_access_expiration_time\\":1681048255}",
              "isBase64Encoded": false
            }
            
            """;
}
