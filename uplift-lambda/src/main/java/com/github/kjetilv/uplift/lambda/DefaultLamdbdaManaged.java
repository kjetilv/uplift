package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.Json;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static java.net.http.HttpClient.Version.HTTP_1_1;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.util.Objects.requireNonNull;

final class DefaultLamdbdaManaged implements LamdbdaManaged {

    private final URI lambdaUri;

    private final LambdaClientSettings settings;

    private final LambdaHandler handler;

    private final ExecutorService lambdaExecutor;

    DefaultLamdbdaManaged(
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
            http(lambdaExecutor, settings);
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings + "]";
    }

    private static Function<HttpRequest, CompletionStage<HttpResponse<InputStream>>> http(
        Executor executor,
        LambdaClientSettings settings
    ) {
        HttpClient.Builder builder = httpClient(executor, settings);
        return request -> {
            try (HttpClient build = builder.build()) {
                return build.sendAsync(request, ofInputStream(), null);
            }
        };
    }

    private static HttpClient.Builder httpClient(Executor executor, LambdaClientSettings settings) {
        HttpClient.Builder builder = HttpClient.newBuilder()
            .version(HTTP_1_1)
            .executor(executor);
        if (settings.hasConnectTimeout()) {
            builder.connectTimeout(settings.connectTimeout());
        }
        return builder;
    }
}
