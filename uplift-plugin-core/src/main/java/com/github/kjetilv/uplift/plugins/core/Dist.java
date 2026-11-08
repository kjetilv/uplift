package com.github.kjetilv.uplift.plugins.core;

import module java.base;
import module java.net.http;

/**
 * The GraalVM distribution can be given as a local file or as a download URL. The template
 * branches on which, so the type is resolved and verified up front.
 */
public final class Dist {

    public static Dist verified(URI uri) {
        Dist dist = new Dist(typeOf(uri), uri);
        dist.verify();
        return dist;
    }

    private final Type type;

    private final URI uri;

    private Dist(Type type, URI uri) {
        this.type = type;
        this.uri = uri;
    }

    public Type type() {
        return type;
    }

    public URI uri() {
        return uri;
    }

    public String uriString() {
        return uri().toASCIIString();
    }

    public Path path() {
        return Path.of(uri);
    }

    private void verify() {
        switch (type) {
            case FILE -> {
                if (!Files.exists(path())) {
                    throw new IllegalStateException("Failed to locate file: " + uri);
                }
            }
            case HTTP -> {
                HttpRequest request = HttpRequest.newBuilder(uri)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
                HttpResponse<Void> response = null;
                try (
                    HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build()
                ) {
                    response = client.send(request, HttpResponse.BodyHandlers.discarding());
                    if (response.statusCode() >= 400) {
                        fail(request, response, null);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail(request, response, e);
                } catch (IllegalStateException e) {
                    throw e;
                } catch (Exception e) {
                    fail(request, response, e);
                }
            }
        }
    }

    private static void fail(
        HttpRequest request,
        HttpResponse<Void> response,
        Exception cause
    ) {
        throw new IllegalStateException(
            "Failed to verify " + request.uri() + ", " + request.method() + " failed: " +
            request + " -> " + response,
            cause
        );
    }

    private static Type typeOf(URI uri) {
        if (uri.getScheme() == null) {
            throw new IllegalStateException("javaDist property must be file/http/https URI: " + uri);
        }
        return switch (uri.getScheme().toLowerCase(Locale.ROOT)) {
            case "file" -> Type.FILE;
            case "http", "https" -> Type.HTTP;
            default -> throw new IllegalStateException(
                "javaDist property must be file/http/https URI: " + uri);
        };
    }

    public enum Type {

        FILE,
        HTTP
    }
}
