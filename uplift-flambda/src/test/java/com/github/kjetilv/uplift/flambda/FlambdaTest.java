package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class FlambdaTest {

    @Test
    void startAndRun() throws Exception {
        var settings = new FlambdaSettings(
            new CorsSettings(
                List.of("*"),
                List.of("GET")
            )
        );

        try (
            var flambda = new Flambda(settings);
            var client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build()
        ) {
            var lambdaUri = flambda.lambdaUri();
            var apiUri = flambda.apiUri();

            System.out.println("Lambda URI: " + lambdaUri);
            System.out.println("API URI: " + apiUri);

            CompletableFuture<HttpResponse<String>> fooFuture = client.sendAsync(
                HttpRequest.newBuilder()
                    .uri(apiUri.resolve("/foo"))
                    .GET().build(),
                HttpResponse.BodyHandlers.ofString()
            ).whenComplete((res, err) -> {
                if (err != null) {
                    System.out.println("Error: " + err);
                }
                System.out.println("Res for foo: " + res + " " + res.body());
            });

            CompletableFuture<HttpResponse<String>> barFuture = client.sendAsync(
                HttpRequest.newBuilder()
                    .uri(apiUri.resolve("/bar"))
                    .GET().build(),
                HttpResponse.BodyHandlers.ofString()
            ).whenComplete((res, err) -> {
                if (err != null) {
                    System.out.println("Error: " + err);
                }
                System.out.println("Res for foo: " + res + " " + res.body());
            });

            for (int i = 0; i < 2; i++) {

                System.out.println("API call started, waiting for Lambda GET...");

                var lambdaGet = client.send(
                    HttpRequest.newBuilder()
                        .uri(lambdaUri.resolve("/"))
                        .GET().build(),
                    HttpResponse.BodyHandlers.ofString()
                );

                System.out.println("Lambda GET status: " + lambdaGet.statusCode());
                System.out.println("Lambda GET headers: " + lambdaGet.headers().map());
                System.out.println("Lambda GET body: `" + lambdaGet.body() + "`");

                var requestId = lambdaGet.headers()
                    .firstValue("lambda-runtime-aws-request-id")
                    .orElseThrow(() -> new IllegalStateException(
                        "Missing lambda-runtime-aws-request-id header"));

                System.out.println("Request ID: " + requestId);

                var responseJson = """
                    {
                        "statusCode": 200,
                        "headers": {"content-type": "text/plain"},
                        "body": "Hello from Lambda",
                        "isBase64Encoded": false,
                        "reqId": "%s"
                    }
                    """.formatted(requestId);

                var postUri = lambdaUri.resolve("/2018-06-01/runtime/invocation/" + requestId + "/response");
                System.out.println("Posting response to: " + postUri);

                HttpResponse<Void> lambdaPost = client.send(
                    HttpRequest.newBuilder()
                        .uri(postUri)
                        .POST(HttpRequest.BodyPublishers.ofString(responseJson))
                        .header("content-type", "application/json")
                        .build(),
                    HttpResponse.BodyHandlers.discarding()
                );

                System.out.println("Lambda POST status: " + lambdaPost.statusCode());
                assertEquals(204, lambdaPost.statusCode());

                System.out.println("Waiting for API response...");
                var apiResponse = fooFuture.get();
                System.out.println("API response status: " + apiResponse.statusCode());
                System.out.println("API response body: " + apiResponse.body());

                assertEquals(200, apiResponse.statusCode());
                assertEquals("Hello from Lambda", apiResponse.body());
            }
        }
    }

    static {
        Flogs.initialize(LogLevel.DEBUG);
    }
}
