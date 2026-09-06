package com.github.kjetilv.uplift.plugins.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * The GraalVM distribution can be given as a local file or as a download URL. The template
 * branches on which, so the type is resolved and verified up front.
 */
public final class Dist {

    public enum Type {
        FILE,
        HTTP
    }

    private final Type type;

    private final URI uri;

    public static Dist verified(URI uri) {
        Dist dist = new Dist(typeOf(uri), uri);
        dist.verify();
        return dist;
    }

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

    public Path path() {
        return Path.of(uri);
    }

    /** Renders as the ASCII form when the type matches, and as null otherwise. */
    public String ifType(Type wanted) {
        return type == wanted ? uri.toASCIIString() : null;
    }

    private void verify() {
        switch (type) {
            case FILE -> {
                if (!Files.exists(path())) {
                    throw new IllegalStateException("Failed to locate file " + uri);
                }
            }
            case HTTP -> {
                try (HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build()) {
                    HttpRequest request = HttpRequest.newBuilder(uri)
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .build();
                    HttpResponse<Void> response =
                        client.send(request, HttpResponse.BodyHandlers.discarding());
                    if (response.statusCode() >= 400) {
                        throw new IllegalStateException(
                            response.statusCode() + ": Failed to fetch: " + uri);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted verifying " + uri, e);
                } catch (IllegalStateException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to fetch: " + uri, e);
                }
            }
        }
    }

    private static Type typeOf(URI uri) {
        String scheme = uri.getScheme() == null
            ? ""
            : uri.getScheme().toLowerCase(Locale.ROOT);
        return switch (scheme) {
            case "file" -> Type.FILE;
            case "http", "https" -> Type.HTTP;
            default -> throw new IllegalStateException(
                "javaDist property must be file/http/https URI: " + uri);
        };
    }
}
