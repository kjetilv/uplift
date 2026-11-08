package com.github.kjetilv.uplift.flambda;

import module java.base;
import module java.net.http;

@SuppressWarnings("unused")
record ReqsImpl(URI uri) implements Reqs {

    @Override
    public Reqs path(URI uri) {
        return new ReqsImpl(this.uri.resolve(uri));
    }

    @Override
    public Reqs path(String uri) {
        return new ReqsImpl(this.uri.resolve(uri));
    }

    @Override
    public CompletableFuture<HttpResponse<String>> execute(String method, String body) {
        return execute(
            method,
            body,
            body != null && looksJsony(body.trim())
        );
    }

    @Override
    public CompletableFuture<HttpResponse<String>> execute(
        String method,
        URI uri,
        Map<String, String> headers,
        String body,
        boolean json
    ) {
        Objects.requireNonNull(method, "method");
        try {
            var resolvedUri = resolve(uri);
            var builder = HttpRequest.newBuilder(resolvedUri);
            if (headers != null) {
                headers.forEach(builder::header);
            }

            var bodyPublisher = body == null || body.isBlank()
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);

            builder.method(method, bodyPublisher);
            if (body != null && json) {
                builder.header("Content-Type", "application/json");
            }

            return CLIENT.sendAsync(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute " + method + " " + uri, e);
        }
    }

    /// Shared by all instances. Closing a client waits for its requests, which made every call synchronous.
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private URI resolve(URI uri) {
        return uri == null ? uri() : uri().resolve(uri);
    }

    private static boolean looksJsony(String body) {
        return contained("{", body, "}") || contained("[", body, "]");
    }

    private static boolean contained(String start, String body, String end) {
        return body.startsWith(start) && body.endsWith(end);
    }
}
