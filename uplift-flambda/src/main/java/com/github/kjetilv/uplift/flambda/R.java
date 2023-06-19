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
public record R(URI uri) {

    public R path(URI uri) {
        return new R(this.uri.resolve(uri));
    }

    public R path(String uri) {
        return new R(this.uri.resolve(uri));
    }

    public CompletableFuture<HttpResponse<String>> req(String method, Map<String, String> headers) {
        return req(method, headers, null, false);
    }

    public CompletableFuture<HttpResponse<String>> req(String method, Object body) {
        return req(method, json(body), true);
    }

    public CompletableFuture<HttpResponse<String>> req(String method, String body) {
        return req(method, body, walksLikeADuck(body.trim()));
    }

    public CompletableFuture<HttpResponse<String>> req(String method, String body, boolean json) {
        return req(method, null, body, json);
    }

    private CompletableFuture<HttpResponse<String>> req(String method) {
        return req(method, false);
    }

    private CompletableFuture<HttpResponse<String>> req(
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

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

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
