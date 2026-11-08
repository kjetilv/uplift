package com.github.kjetilv.uplift.plugins.core;

import module java.base;
import module java.net.http;

/**
 * The GraalVM distribution can be given as a local file or as a download URL. The template
 * branches on which, so the type is resolved and verified up front.
 */
public record Dist(Type type, URI uri) {

    public static Dist verified(URI uri) {
        return new Dist(typeOf(uri), uri);
    }

    public String uriString() {
        return uri().toASCIIString();
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
            throw new IllegalStateException("property must be file/http/https URI: " + uri);
        }
        var type = switch (uri.getScheme().toLowerCase(Locale.ROOT)) {
            case "file" -> Type.FILE;
            case "http", "https" -> Type.HTTP;
            default -> throw new IllegalStateException(
                "javaDist property must be file/http/https URI: " + uri);
        };
        return type.verified(uri);
    }

    public enum Type {

        FILE {
            @Override
            Type verified(URI uri) {
                if (!Files.exists(Path.of(uri))) {
                    throw new IllegalStateException("Failed to locate file: " + uri);
                }
                return this;
            }
        },
        HTTP {
            @Override
            Type verified(URI uri) {
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
                return this;
            }
        };

        abstract Type verified(URI uri);
    }
}
