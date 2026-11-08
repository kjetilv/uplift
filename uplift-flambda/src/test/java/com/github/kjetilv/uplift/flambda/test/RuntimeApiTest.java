package com.github.kjetilv.uplift.flambda.test;

import module java.base;
import com.github.kjetilv.uplift.flambda.Flambda;
import com.github.kjetilv.uplift.flambda.FlambdaSettings;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.synchttp.CorsSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/// Tests Flambda over raw HTTP, with the test acting as the Lambda runtime.
class RuntimeApiTest {

    static final int TIMEOUT_STATUS = 504;

    static final int FUNCTION_ERROR_STATUS = 502;

    static final int MALFORMED_STATUS = 502;

    static final String NEXT = "/2018-06-01/runtime/invocation/next";

    static final Instant NOW = Instant.parse("2026-03-12T19:03:58.390Z");

    private Flambda flambda;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @BeforeEach
    void setUp() {
        flambda = new Flambda(settings(2));
    }

    @AfterEach
    void tearDown() {
        assertThat(flambda.pendingRequests()).isZero();
        flambda.close();
        executor.close();
    }

    @Test
    void apiCallWithoutLambdaTimesOut() {
        var start = System.nanoTime();
        var response = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n").join();
        assertThat(status(response)).isEqualTo(TIMEOUT_STATUS);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(3));
    }

    @Test
    void responseIsAccepted() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        var id = id(next());
        var posted = postResponse(id, "{\"statusCode\":200,\"body\":\"hello\"}");
        assertThat(status(posted)).isEqualTo(202);
        var response = apiCall.join();
        assertThat(status(response)).isEqualTo(200);
        assertThat(body(response)).isEqualTo("hello");
    }

    @Test
    void malformedResponseGivesGatewayError() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        var id = id(next());
        assertThat(status(postResponse(id, "not json"))).isEqualTo(202);
        assertThat(status(apiCall.join())).isEqualTo(MALFORMED_STATUS);
    }

    @Test
    void functionErrorGivesGatewayError() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        var id = id(next());
        var posted = post(
            "/2018-06-01/runtime/invocation/" + id + "/error",
            "{\"errorMessage\":\"boom\",\"errorType\":\"Runtime.Error\"}"
        );
        assertThat(status(posted)).isEqualTo(202);
        assertThat(status(apiCall.join())).isEqualTo(FUNCTION_ERROR_STATUS);
    }

    @Test
    void initErrorIsAccepted() {
        assertThat(status(post("/2018-06-01/runtime/init/error", "{\"errorMessage\":\"x\"}"))).isEqualTo(202);
    }

    @Test
    void abandonedPollDoesNotHangCaller() throws Exception {
        try (var socket = new Socket()) {
            socket.connect(flambda.lambda());
            socket.getOutputStream().write(("GET " + NEXT + " HTTP/1.1\r\nHost: l\r\n\r\n").getBytes(UTF_8));
            Thread.sleep(200);
        }
        var start = System.nanoTime();
        var response = api("GET /lost HTTP/1.1\r\nHost: a\r\n\r\n").join();
        assertThat(status(response)).isEqualTo(TIMEOUT_STATUS);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(3));

        var apiCall = api("GET /after HTTP/1.1\r\nHost: a\r\n\r\n");
        var id = id(next());
        postResponse(id, "{\"statusCode\":200,\"body\":\"after\"}");
        assertThat(body(apiCall.join())).isEqualTo("after");
    }

    @Test
    void tooLargeResponseFailsCallerAtOnce() {
        var start = System.nanoTime();
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        var id = id(next());
        var posted = raw(
            flambda.lambda(),
            "POST /2018-06-01/runtime/invocation/" + id + "/response HTTP/1.1\r\nHost: l\r\nContent-Length: 7000000\r\n\r\n"
        );
        assertThat(status(posted)).isEqualTo(413);
        assertThat(status(apiCall.join())).isEqualTo(MALFORMED_STATUS);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(1));
    }

    @Test
    void unknownRuntimePathIsNotFound() {
        var start = System.nanoTime();
        assertThat(status(raw(flambda.lambda(), "GET /health HTTP/1.1\r\nHost: l\r\n\r\n"))).isEqualTo(404);
        assertThat(status(post("/2018-06-01/runtime/other", "{}"))).isEqualTo(404);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(2));
    }

    @Test
    void responseForUnknownIdIsRejected() {
        assertThat(status(postResponse("AAAAAAAAAAAAAAAAAAAAAA", "{\"statusCode\":200}"))).isEqualTo(400);
    }

    @Test
    void lateResponseIsRejected() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        var id = id(next());
        assertThat(status(apiCall.join())).isEqualTo(TIMEOUT_STATUS);
        assertThat(status(postResponse(id, "{\"statusCode\":200,\"body\":\"late\"}"))).isEqualTo(400);
    }

    @Test
    void secondResponseIsRejected() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        var id = id(next());
        assertThat(status(postResponse(id, "{\"statusCode\":200,\"body\":\"one\"}"))).isEqualTo(202);
        assertThat(status(postResponse(id, "{\"statusCode\":200,\"body\":\"two\"}"))).isEqualTo(400);
        assertThat(body(apiCall.join())).isEqualTo("one");
    }

    @Test
    void fullQueueThrottles() {
        var queued = List.of(api("GET /1 HTTP/1.1\r\nHost: a\r\n\r\n"), api("GET /2 HTTP/1.1\r\nHost: a\r\n\r\n"));
        pause(300);
        var throttled = api("GET /3 HTTP/1.1\r\nHost: a\r\n\r\n").join();
        assertThat(status(throttled)).isEqualTo(429);
        queued.forEach(CompletableFuture::join);
    }

    @Test
    void lambdaHeadersAreNotDuplicated() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\nOrigin: https://x\r\n\r\n");
        var id = id(next());
        postResponse(id, """
            {"statusCode":200,"headers":{"Content-Length":"2","Access-Control-Allow-Origin":"https://y","X-Own":"1"},"body":"ok"}""");
        var response = apiCall.join();
        assertThat(headerLines(response, "content-length")).hasSize(1);
        assertThat(headerLines(response, "access-control-allow-origin")).hasSize(1);
        assertThat(headerLines(response, "x-own")).containsExactly("1");
        assertThat(body(response)).isEqualTo("ok");
    }

    @Test
    void closeReleasesWaiters() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        var id = id(next());
        assertThat(id).isNotNull();
        var poll = CompletableFuture.supplyAsync(
            () -> raw(flambda.lambda(), "GET " + NEXT + " HTTP/1.1\r\nHost: l\r\n\r\n"),
            executor
        );
        pause(300);
        var start = System.nanoTime();
        flambda.close();
        apiCall.join();
        poll.join();
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(2));
    }

    @Test
    void eventFormat() {
        var apiCall = api("""
            POST /p/a%20b?x=1&x=2&y=%C3%A5 HTTP/1.1\r
            Host: a\r
            User-Agent: probe/1\r
            X-Multi: 1\r
            X-Multi: 2\r
            Cookie: c1=v1; c2=v2\r
            Content-Type: text/plain\r
            Content-Length: 5\r
            \r
            hello""");
        var nextResponse = next();
        var event = json(body(nextResponse));
        postResponse(id(nextResponse), "{\"statusCode\":200}");
        apiCall.join();

        assertThat(event).containsEntry("version", "2.0");
        assertThat(event).containsEntry("routeKey", "$default");
        assertThat(event).containsEntry("rawPath", "/p/a%20b");
        assertThat(event).containsEntry("rawQueryString", "x=1&x=2&y=%C3%A5");
        assertThat(event).containsEntry("cookies", List.of("c1=v1", "c2=v2"));
        assertThat(event).containsEntry("body", "hello");
        assertThat(event).containsEntry("isBase64Encoded", false);
        @SuppressWarnings("unchecked") var headers = (Map<String, Object>) event.get("headers");
        assertThat(headers).containsEntry("x-multi", "1,2");
        assertThat(headers).containsEntry("user-agent", "probe/1");
        assertThat(headers).doesNotContainKey("cookie");
        @SuppressWarnings("unchecked") var query = (Map<String, Object>) event.get("queryStringParameters");
        assertThat(query).containsEntry("x", "1,2");
        assertThat(query).containsEntry("y", "å");
        @SuppressWarnings("unchecked") var context = (Map<String, Object>) event.get("requestContext");
        assertThat(context).containsEntry("routeKey", "$default");
        assertThat(context).containsEntry("stage", "$default");
        assertThat(context).containsEntry("time", "12/Mar/2026:19:03:58 +0000");
        assertThat(((Number) context.get("timeEpoch")).longValue()).isEqualTo(NOW.toEpochMilli());
        assertThat(context.get("requestId")).isNotNull();
        @SuppressWarnings("unchecked") var http = (Map<String, Object>) context.get("http");
        assertThat(http).containsEntry("method", "POST");
        assertThat(http).containsEntry("path", "/p/a%20b");
        assertThat(http).containsEntry("protocol", "HTTP/1.1");
        assertThat(http).containsEntry("userAgent", "probe/1");
        assertThat(http.get("sourceIp")).isNotNull();
    }

    @Test
    void binaryBodyIsBase64() throws Exception {
        byte[] bytes = {(byte) 0xff, 0x00, (byte) 0xfe, 0x01};
        var apiCall = CompletableFuture.supplyAsync(() -> {
            var head = "POST /bin HTTP/1.1\r\nHost: a\r\nContent-Type: application/octet-stream\r\nContent-Length: 4\r\n\r\n";
            var request = new ByteArrayOutputStream();
            request.writeBytes(head.getBytes(UTF_8));
            request.writeBytes(bytes);
            return raw(flambda.api(), request.toByteArray());
        }, executor);
        var nextResponse = next();
        var event = json(body(nextResponse));
        postResponse(id(nextResponse), "{\"statusCode\":200}");
        apiCall.join();
        assertThat(event).containsEntry("isBase64Encoded", true);
        assertThat(Base64.getDecoder().decode((String) event.get("body"))).isEqualTo(bytes);
    }

    @Test
    void runtimeHeaders() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        var nextResponse = next();
        postResponse(id(nextResponse), "{\"statusCode\":200}");
        apiCall.join();
        assertThat(headerLines(nextResponse, "lambda-runtime-deadline-ms"))
            .singleElement()
            .satisfies(deadline ->
                assertThat(Long.parseLong(deadline)).isEqualTo(NOW.plusSeconds(1).toEpochMilli()));
        assertThat(headerLines(nextResponse, "lambda-runtime-invoked-function-arn"))
            .singleElement()
            .satisfies(arn -> assertThat(arn).startsWith("arn:aws:lambda:").endsWith(":function:runtimeApiTest"));
        assertThat(headerLines(nextResponse, "lambda-runtime-trace-id"))
            .singleElement()
            .satisfies(trace -> assertThat(trace).startsWith("Root=1-"));
    }

    @Test
    void responseWithoutStatusCodeIsJson() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        postResponse(id(next()), "{\"hello\":\"world\"}");
        var response = apiCall.join();
        assertThat(status(response)).isEqualTo(200);
        assertThat(headerLines(response, "content-type")).containsExactly("application/json");
        assertThat(body(response)).isEqualTo("{\"hello\":\"world\"}");
    }

    @Test
    void responseCookiesAreSetCookieHeaders() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        postResponse(id(next()), "{\"statusCode\":200,\"cookies\":[\"a=1; Path=/\",\"b=2\"],\"body\":\"ok\"}");
        assertThat(headerLines(apiCall.join(), "set-cookie")).containsExactly("a=1; Path=/", "b=2");
    }

    @Test
    void noContentDropsBody() {
        var apiCall = api("GET /x HTTP/1.1\r\nHost: a\r\n\r\n");
        postResponse(id(next()), "{\"statusCode\":204,\"body\":\"ignored\"}");
        var response = apiCall.join();
        assertThat(status(response)).isEqualTo(204);
        assertThat(body(response)).isEmpty();
    }

    @Test
    void headIsForwarded() {
        var apiCall = api("HEAD /health HTTP/1.1\r\nHost: a\r\n\r\n");
        var nextResponse = next();
        var event = json(body(nextResponse));
        postResponse(id(nextResponse), "{\"statusCode\":200,\"body\":\"not sent\"}");
        var response = apiCall.join();
        @SuppressWarnings("unchecked") var context = (Map<String, Object>) event.get("requestContext");
        @SuppressWarnings("unchecked") var http = (Map<String, Object>) context.get("http");
        assertThat(http).containsEntry("method", "HEAD");
        assertThat(status(response)).isEqualTo(200);
        assertThat(body(response)).isEmpty();
    }

    @Test
    void tooLargeRequestIsRejected() {
        var body = "x".repeat(2000);
        var response = api("POST /big HTTP/1.1\r\nHost: a\r\nContent-Length: 2000\r\n\r\n" + body).join();
        assertThat(status(response)).isEqualTo(413);
    }

    @Test
    void serverBindsToConfiguredAddress() {
        assertThat(flambda.api().getAddress()).isEqualTo(InetAddress.getLoopbackAddress());
        assertThat(flambda.lambdaUri().getPath()).isEqualTo(NEXT);
    }

    static FlambdaSettings settings(int queueLength) {
        return new FlambdaSettings(
            "runtimeApiTest",
            InetAddress.getLoopbackAddress(),
            null,
            null,
            1024,
            queueLength,
            Duration.ofSeconds(1),
            new CorsSettings(List.of("*"), List.of("GET")),
            () -> NOW
        );
    }

    private CompletableFuture<String> api(String request) {
        return CompletableFuture.supplyAsync(() -> raw(flambda.api(), request), executor);
    }

    private String next() {
        return raw(flambda.lambda(), "GET " + NEXT + " HTTP/1.1\r\nHost: l\r\n\r\n");
    }

    private String postResponse(String id, String json) {
        return post("/2018-06-01/runtime/invocation/" + id + "/response", json);
    }

    private String post(String path, String json) {
        return raw(
            flambda.lambda(),
            "POST " + path + " HTTP/1.1\r\nHost: l\r\nContent-Length: " + json.getBytes(UTF_8).length + "\r\n\r\n" + json
        );
    }

    static String raw(InetSocketAddress address, String request) {
        return raw(address, request.getBytes(UTF_8));
    }

    /// Sends one request with `Connection: close` added, and reads until the server closes or 5 s pass
    static String raw(InetSocketAddress address, byte[] request) {
        var text = new String(request, StandardCharsets.ISO_8859_1);
        var headEnd = text.indexOf("\r\n\r\n");
        var withClose = new ByteArrayOutputStream();
        withClose.writeBytes(text.substring(0, headEnd).getBytes(StandardCharsets.ISO_8859_1));
        withClose.writeBytes("\r\nConnection: close".getBytes(UTF_8));
        withClose.writeBytes(Arrays.copyOfRange(request, headEnd, request.length));
        try (var socket = new Socket()) {
            socket.connect(address);
            socket.setSoTimeout(5000);
            socket.getOutputStream().write(withClose.toByteArray());
            try {
                return new String(socket.getInputStream().readAllBytes(), UTF_8);
            } catch (SocketTimeoutException e) {
                return "<TIMEOUT>";
            }
        } catch (IOException e) {
            return "<" + e + ">";
        }
    }

    static int status(String response) {
        try {
            return Integer.parseInt(response.substring(9, 12));
        } catch (Exception e) {
            return -1;
        }
    }

    static String body(String response) {
        var index = response.indexOf("\r\n\r\n");
        return index < 0 ? response : response.substring(index + 4);
    }

    static List<String> headerLines(String response, String name) {
        var index = response.indexOf("\r\n\r\n");
        return (index < 0 ? response : response.substring(0, index)).lines()
            .skip(1)
            .filter(line -> line.regionMatches(true, 0, name + ":", 0, name.length() + 1))
            .map(line -> line.substring(name.length() + 1).strip())
            .toList();
    }

    static String id(String nextResponse) {
        return headerLines(nextResponse, "lambda-runtime-aws-request-id").stream().findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> json(String json) {
        return (Map<String, Object>) Json.instance().read(json);
    }

    static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
