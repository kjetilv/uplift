package com.github.kjetilv.uplift.lambda;

import module java.base;
import module java.net.http;

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
        this.client = httpClient(this.settings).build();
    }

    @Override
    public LambdaLooper<HttpRequest, HttpResponse<InputStream>> looper() {
        Function<HttpRequest, CompletionStage<HttpResponse<InputStream>>> fetch = request ->
            this.client.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofInputStream(),
                null
            );
        InvocationSource<HttpRequest, HttpResponse<InputStream>> source =
            new HttpInvocationSource(
                fetch,
                lambdaUri,
                settings.responseTimeout(),
                settings.time()
            );
        InvocationSink<HttpRequest, HttpResponse<InputStream>> sink =
            new HttpInvocationSink(
                fetch,
                settings.time()
            );
        return LambdaLoopers.looper(handler, source, sink, settings.time());
    }

    @Override
    public void close() {
        client.close();
    }

    private static final ExecutorService VIRTUAL_THREADS = Executors.newSingleThreadExecutor();

    private static HttpClient.Builder httpClient(LambdaClientSettings settings) {
        return settings.hasConnectTimeout()
            ? builder().connectTimeout(settings.connectTimeout())
            : builder();
    }

    private static HttpClient.Builder builder() {
        return HttpClient.newBuilder().version(HTTP_1_1).executor(VIRTUAL_THREADS);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings + "]";
    }
}
