package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.LogLevel;
import com.github.kjetilv.uplift.lambda.RequestOutRW;
import com.github.kjetilv.uplift.lambda.ResponseIn;
import com.github.kjetilv.uplift.lambda.ResponseInRW;
import com.github.kjetilv.uplift.synchttp.CorsSettings;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlambdaTest {

    @Test
    void startAndRun() throws Exception {
        var settings = new FlambdaSettings(
            "flambdaTest",
            new CorsSettings(
                List.of("*"),
                List.of("GET")
            )
        );

        CompletableFuture<HttpResponse<String>> fooFuture;
        CompletableFuture<HttpResponse<String>> barFuture;

        try (
            var flambda = new Flambda(settings);
            var client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build()
        ) {
            var lambdaUri = flambda.lambdaUri();
            var apiUri = flambda.apiUri();

            fooFuture = client.sendAsync(
                HttpRequest.newBuilder()
                    .uri(apiUri.resolve("/foo"))
                    .GET().build(),
                HttpResponse.BodyHandlers.ofString()
            );

            barFuture = client.sendAsync(
                HttpRequest.newBuilder()
                    .uri(apiUri.resolve("/bar"))
                    .GET().build(),
                HttpResponse.BodyHandlers.ofString()
            );

            for (int i = 0; i < 2; i++) {

                var lambdaGet = getRequest(client, lambdaUri);
                assertThat(lambdaGet.statusCode()).isEqualTo(200);
                System.out.println("Lambda GET status: " + lambdaGet.statusCode());
                System.out.println("Lambda GET headers: " + lambdaGet.headers()
                    .map());
                System.out.println("Lambda GET body: `" + lambdaGet.body() + "`");

                var requestOut = RequestOutRW.INSTANCE.stringReader().read(lambdaGet.body());
                var requestId = lambdaGet.headers()
                    .firstValue("lambda-runtime-aws-request-id")
                    .orElseThrow(() -> new IllegalStateException(
                        "Missing lambda-runtime-aws-request-id header"));

                var responseIn = new ResponseIn(
                    200,
                    Map.of("content-type", "text/plain"),
                    "Hello " + requestOut.path(),
                    false,
                    requestId
                );

                System.out.println("Request ID: " + requestId);
                postResponse(
                    client, lambdaUri, responseIn
                );
            }
        }

        var apiResponse1 = fooFuture.get();
        assertThat(apiResponse1.statusCode()).isEqualTo(200);
        assertThat(apiResponse1.body()).isEqualTo("Hello /foo");

        var apiResponse2 = barFuture.get();
        assertThat(apiResponse2.statusCode()).isEqualTo(200);
        assertThat(apiResponse2.body()).isEqualTo("Hello /bar");
    }

    static {
        Flogs.initialize(LogLevel.DEBUG);
    }

    private static HttpResponse<String> getRequest(HttpClient client, URI lambdaUri)
        throws IOException, InterruptedException {
        var lambdaGet = client.send(
            HttpRequest.newBuilder()
                .uri(lambdaUri.resolve("/"))
                .GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        return lambdaGet;
    }

    private static void postResponse(HttpClient client, URI lambdaUri, ResponseIn response)
        throws IOException, InterruptedException {
        var postUri = lambdaUri.resolve("/2018-06-01/runtime/invocation/" + response.reqId() + "/response");
        System.out.println("Posting response to: " + postUri);
        HttpResponse<Void> lambdaPost = client.send(
            HttpRequest.newBuilder()
                .uri(postUri)
                .POST(HttpRequest.BodyPublishers.ofString(ResponseInRW.INSTANCE.stringWriter().write(response)))
                .header("content-type", "application/json")
                .build(),
            HttpResponse.BodyHandlers.discarding()
        );
        System.out.println("Lambda POST status: " + lambdaPost.statusCode());
        assertEquals(204, lambdaPost.statusCode());
    }
}
