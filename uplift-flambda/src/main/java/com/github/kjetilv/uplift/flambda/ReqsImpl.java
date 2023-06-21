package com.github.kjetilv.uplift.flambda;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
    public CompletableFuture<HttpResponse<String>> execute(String method, Object body) {
        return execute(method, json(body), true);
    }

    @Override
    public CompletableFuture<HttpResponse<String>> execute(String method, String body) {
        return execute(method, body, walksLikeADuck(body.trim()));
    }

    @Override
    public CompletableFuture<HttpResponse<String>> execute(
        String method, Map<String, String> headers, String body, boolean json
    ) {
        try {
            HttpRequest.Builder base = HttpRequest.newBuilder(uri);
            if (headers != null) {
                headers.forEach(base::header);
            }
            if (body == null) {
                base.GET();
            } else {
                base.method(method, HttpRequest.BodyPublishers.ofString(body));
            }
            if (body != null && json) {
                base.header("Content-Type", "application/json");
            }
            return HttpClient.newBuilder().build().sendAsync(base.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private static String json(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write: " + value, e);
        }
    }

    private static boolean walksLikeADuck(String body) {
        return contained("{", body, "}") || contained("[", body, "]");
    }

    private static boolean contained(String start, String body, String end) {
        return body.startsWith(start) && body.endsWith(end);
    }
}
