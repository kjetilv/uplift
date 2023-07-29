package com.github.kjetilv.uplift.lambda;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import com.github.kjetilv.uplift.json.Json;

import static java.net.http.HttpClient.Version.HTTP_1_1;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.util.Objects.requireNonNull;

public final class DefaultLamdbdaManaged implements LamdbdaManaged {

    private final URI lambdaUri;

    private final LambdaClientSettings settings;

    private final LambdaHandler handler;

    private final ExecutorService lambdaExecutor;

    public DefaultLamdbdaManaged(
        URI lambdaUri,
        LambdaClientSettings settings,
        LambdaHandler handler,
        ExecutorService lambdaExecutor
    ) {
        this.lambdaUri = requireNonNull(lambdaUri, "lambdaUri");
        this.settings = requireNonNull(settings, "settings");
        this.handler = requireNonNull(handler, "handler");
        this.lambdaExecutor = requireNonNull(lambdaExecutor, "lambdaExecutor");
    }

    @Override
    public LambdaHandler handler() {
        return handler;
    }

    @Override
    public LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper() {
        Function<HttpRequest, CompletionStage<HttpResponse<InputStream>>> client =
            http(lambdaExecutor, settings.connectTimeout());
        InvocationSource<HttpRequest, HttpResponse<InputStream>> source =
            new HttpInvocationSource(
                client,
                lambdaUri,
                settings.responseTimeout(),
                Json.INSTANCE::jsonMap,
                settings.time()
            );
        InvocationSink<HttpRequest, HttpResponse<InputStream>> sink =
            new HttpInvocationSink(
                client,
                settings.time()
            );
        return LambdaLoopers.looper(handler, source, sink, settings.time());
    }

    private static Function<HttpRequest, CompletionStage<HttpResponse<InputStream>>> http(
        Executor executor,
        Duration connectTimeout
    ) {
        HttpClient httpClient = httpClient(executor, connectTimeout);
        return request ->
            httpClient.sendAsync(request, ofInputStream(), null);
    }

    private static HttpClient httpClient(
        Executor executor,
        Duration connectTimeout
    ) {
        HttpClient.Builder builder = HttpClient.newBuilder()
            .version(HTTP_1_1)
            .executor(executor);
        if (connectTimeout.compareTo(Duration.ZERO) > 0) {
            builder.connectTimeout(connectTimeout);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings + "]";
    }
}
