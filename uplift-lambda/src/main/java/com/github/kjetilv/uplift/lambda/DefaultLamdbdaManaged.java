package com.github.kjetilv.uplift.lambda;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.net.http.HttpClient.Version.HTTP_1_1;
import static java.util.Objects.requireNonNull;

final class DefaultLamdbdaManaged implements LamdbdaManaged {

    private final URI lambdaUri;

    private final LambdaClientSettings settings;

    private final LambdaHandler handler;

    private final HttpClient client;

    DefaultLamdbdaManaged(URI lambdaUri, LambdaClientSettings settings, LambdaHandler handler) {
        this.lambdaUri = requireNonNull(lambdaUri, "lambdaUri");
        this.settings = requireNonNull(settings, "settings");
        this.handler = requireNonNull(handler, "handler");
        this.client = httpClient(settings).build();
    }

    @Override
    public LambdaHandler handler() {
        return handler;
    }

    @Override
    public LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper() {
        Function<HttpRequest, CompletionStage<HttpResponse<InputStream>>> client = request ->
            this.client.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofInputStream(),
                null
            );
        InvocationSource<HttpRequest, HttpResponse<InputStream>> source =
            new HttpInvocationSource(
                client,
                lambdaUri,
                settings.responseTimeout(),
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
    public void close() {
        client.close();
    }

    private static final ExecutorService VIRTUAL = Executors.newSingleThreadExecutor();

    private static HttpClient.Builder httpClient(LambdaClientSettings settings) {
        HttpClient.Builder builder = HttpClient.newBuilder()
            .version(HTTP_1_1)
            .executor(VIRTUAL);
        if (settings.hasConnectTimeout()) {
            builder.connectTimeout(settings.connectTimeout());
        }
        return builder;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings + "]";
    }
}
