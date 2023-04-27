package com.github.kjetilv.uplift.lambda;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.github.kjetilv.uplift.json.Json;

import static java.net.http.HttpClient.Version.HTTP_1_1;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.util.Objects.requireNonNull;

public final class DefaultLamdbdaManaged implements LamdbdaManaged {

    private final URI lambdaUri;

    private final LambdaClientSettings settings;

    private final LambdaHandler handler;

    public DefaultLamdbdaManaged(URI lambdaUri, LambdaClientSettings settings, LambdaHandler handler) {
        this.lambdaUri = requireNonNull(lambdaUri, "lambdaUri");
        this.settings = requireNonNull(settings, "settings");
        this.handler = requireNonNull(handler, "handler");
    }

    @Override
    public LambdaHandler handler() {
        return handler;
    }

    @Override
    public LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper() {
        Executor executor = settings.lambdaExecutor();
        Function<HttpRequest, CompletionStage<HttpResponse<InputStream>>> client =
            http(executor, settings.connectTimeout());
        InvocationSource<HttpRequest, HttpResponse<InputStream>> source =
            new HttpInvocationSource(
                client,
                lambdaUri,
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
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .version(HTTP_1_1)
            .executor(executor)
            .build();
        return request ->
            httpClient.sendAsync(request, ofInputStream(), null);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings + "]";
    }
}
