package com.github.kjetilv.uplift.s3.test;

import module java.base;
import module java.net.http;

/// [HttpClient] that records requests and answers them with a responder function.
final class FakeHttpClient extends HttpClient {

    record Exchange(HttpRequest request, String body) {
    }

    record Reply(int status, String body) {
    }

    @FunctionalInterface
    interface Responder {

        Reply reply(Exchange exchange) throws IOException;
    }

    private final Responder responder;

    private final List<Exchange> exchanges = new CopyOnWriteArrayList<>();

    FakeHttpClient(Responder responder) {
        this.responder = responder;
    }

    List<Exchange> exchanges() {
        return List.copyOf(exchanges);
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
        throws IOException {
        var exchange = new Exchange(request, requestBody(request));
        exchanges.add(exchange);
        var reply = responder.reply(exchange);
        var info = new HttpResponse.ResponseInfo() {

            @Override
            public int statusCode() {
                return reply.status();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(Map.of(), (_, _) -> true);
            }

            @Override
            public Version version() {
                return Version.HTTP_1_1;
            }
        };
        var subscriber = responseBodyHandler.apply(info);
        HttpRequest.BodyPublishers.ofString(reply.body()).subscribe(new Flow.Subscriber<>() {

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscriber.onSubscribe(subscription);
            }

            @Override
            public void onNext(ByteBuffer item) {
                subscriber.onNext(List.of(item));
            }

            @Override
            public void onError(Throwable throwable) {
                subscriber.onError(throwable);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        });
        var body = subscriber.getBody().toCompletableFuture().join();
        return new HttpResponse<>() {

            @Override
            public int statusCode() {
                return reply.status();
            }

            @Override
            public HttpRequest request() {
                return request;
            }

            @Override
            public Optional<HttpResponse<T>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return info.headers();
            }

            @Override
            public T body() {
                return body;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return request.uri();
            }

            @Override
            public Version version() {
                return Version.HTTP_1_1;
            }
        };
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request,
        HttpResponse.BodyHandler<T> responseBodyHandler
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request,
        HttpResponse.BodyHandler<T> responseBodyHandler,
        HttpResponse.PushPromiseHandler<T> pushPromiseHandler
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
        return Redirect.NEVER;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
        return null;
    }

    @Override
    public SSLParameters sslParameters() {
        return null;
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public Version version() {
        return Version.HTTP_1_1;
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }

    private static String requestBody(HttpRequest request) {
        var collected = new ByteArrayOutputStream();
        request.bodyPublisher().ifPresent(publisher ->
            publisher.subscribe(new Flow.Subscriber<>() {

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(ByteBuffer item) {
                    var bytes = new byte[item.remaining()];
                    item.get(bytes);
                    collected.writeBytes(bytes);
                }

                @Override
                public void onError(Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onComplete() {
                }
            }));
        return collected.toString(StandardCharsets.UTF_8);
    }
}
