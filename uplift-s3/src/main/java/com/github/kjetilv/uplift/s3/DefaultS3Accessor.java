package com.github.kjetilv.uplift.s3;

import module java.base;
import module java.net.http;
import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.io.Range;
import com.github.kjetilv.uplift.s3.auth.AwsAuthHeaderSigner;
import com.github.kjetilv.uplift.s3.auth.AwsAuthQueryParamSigner;
import com.github.kjetilv.uplift.s3.util.UriEncoding;
import com.github.kjetilv.uplift.s3.util.Xml;
import com.github.kjetilv.uplift.util.Maps;
import com.github.kjetilv.uplift.util.Print;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.kjetilv.uplift.s3.auth.Hashes.md5;
import static com.github.kjetilv.uplift.s3.auth.Hashes.sha256;
import static java.util.Objects.requireNonNull;

public final class DefaultS3Accessor implements S3Accessor {

    private static final Logger log = LoggerFactory.getLogger(DefaultS3Accessor.class);

    private final String accessKey;

    private final String secretKey;

    private final String sessionToken;

    private final String bucket;

    private final String region;

    private final HttpClient httpClient;

    private final InstantSource time;

    public DefaultS3Accessor(Env env, HttpClient httpClient, String bucket, String region) {
        this(env, httpClient, bucket, region, InstantSource.system());
    }

    public DefaultS3Accessor(Env env, HttpClient httpClient, String bucket, String region, InstantSource time) {
        requireNonNull(env, "env");
        this.httpClient = requireNonNull(httpClient, "httpClient");
        this.accessKey = requireNonNull(env.accessKey(), "Env.accessKey()");
        this.secretKey = requireNonNull(env.secretKey(), "Env.secretKey()");
        this.sessionToken = env.sessionToken();
        this.bucket = requireNonNull(bucket, "bucket");
        this.region = region == null || region.isBlank() ? "eu-north-1" : region;
        this.time = requireNonNull(time, "time");

        log.debug("{} created", this);
    }

    @Override
    public Optional<URI> presign(String name, Duration timeToLive) {
        var expiresIn = timeToLive.toSeconds();
        if (expiresIn < 1 || expiresIn > MAX_PRESIGN_SECONDS) {
            throw new IllegalArgumentException(
                "Presigned URL lifetime must be between 1 second and 7 days: " + timeToLive);
        }
        var endpointUri = URI.create(endpoint(name));

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("X-Amz-Expires", String.valueOf(expiresIn));
        if (sessionToken != null) {
            queryParams.put("X-Amz-Security-Token", sessionToken);
        }
        var query = new AwsAuthQueryParamSigner(endpointUri, "GET", "s3", region)
            .computeSignature(
                time.instant(),
                new HashMap<>(),
                queryParams,
                UNSIGNED_PAYLOAD,
                accessKey,
                secretKey
            );
        return Optional.of(URI.create(endpointUri + "?" + query));
    }

    @Override
    public Optional<? extends InputStream> stream(String name, Range range) {
        return send(getObjectRequest(name, range), HttpResponse.BodyHandlers.ofInputStream());
    }

    @Override
    public void put(String remoteName, byte[] bytes) {
        sendRequired(putObjectRequest(remoteName, bytes), HttpResponse.BodyHandlers.replacing(remoteName));
    }

    @Override
    public Map<String, RemoteInfo> remoteInfos(String prefix) {
        return Maps.indexBy(
            Stream.iterate(
                    listPage(prefix, null),
                    Objects::nonNull,
                    page -> page.continuationToken()
                        .map(token -> listPage(prefix, token))
                        .orElse(null)
                )
                .flatMap(page -> page.remoteInfos()
                    .stream())
                .toList(),
            RemoteInfo::key
        );
    }

    @Override
    public void remove(Collection<String> objects) {
        var keys = List.copyOf(objects);
        var batches = (keys.size() + MAX_DELETE_KEYS - 1) / MAX_DELETE_KEYS;
        var errors = IntStream.range(0, batches)
            .mapToObj(batch ->
                keys.subList(batch * MAX_DELETE_KEYS, Math.min(keys.size(), (batch + 1) * MAX_DELETE_KEYS)))
            .map(this::deleteObjectRequest)
            .map(request ->
                sendRequired(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)))
            .flatMap(DefaultS3Accessor::deleteErrors)
            .toList();
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Failed to delete " + errors.size() + " objects: " + errors);
        }
    }

    private ListPage listPage(String prefix, String continuationToken) {
        return send(
            listObjectsRequest(prefix, continuationToken),
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        )
            .map(DefaultS3Accessor::parseListPage)
            .orElseGet(() -> new ListPage(List.of(), Optional.empty()));
    }

    private <T> T sendRequired(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) {
        return send(request, bodyHandler)
            .orElseThrow(() -> new IllegalStateException("Not found: " + request));
    }

    private <T> Optional<T> send(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) {
        return switch (sendWithRetries(request, outcomeHandler(bodyHandler), 0, null)) {
            case Outcome.Ok<T>(var body) -> Optional.of(body);
            case Outcome.NotFound<T> _ -> Optional.empty();
            case Outcome.Failed<T>(var status, var body) -> throw new IllegalStateException(
                "Failed to get " + request + " => " + status + ": " + body);
        };
    }

    private <T> Outcome<T> sendWithRetries(
        HttpRequest request,
        HttpResponse.BodyHandler<Outcome<T>> bodyHandler,
        int attempt,
        Exception previous
    ) {
        try {
            var outcome = httpClient.send(request, bodyHandler).body();
            if (outcome instanceof Outcome.Failed<T>(var status, var body) &&
                RETRYABLE_STATUSES.contains(status) &&
                attempt < MAX_RETRIES
            ) {
                log.debug("Retrying {} after {}: {}", request, status, body);
                backoff(request, attempt);
                return sendWithRetries(request, bodyHandler, attempt + 1, previous);
            }
            return outcome;
        } catch (IOException e) {
            if (previous != null) {
                e.addSuppressed(previous);
            }
            if (attempt < MAX_RETRIES) {
                log.debug("Retrying {} after {}", request, e.toString());
                backoff(request, attempt);
                return sendWithRetries(request, bodyHandler, attempt + 1, e);
            }
            throw new IllegalStateException("Failed to query for " + request, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while querying for " + request, e);
        }
    }

    private HttpRequest listObjectsRequest(String prefix, String continuationToken) {
        return buildRequest(
            "GET",
            "",
            Stream.of(
                    Optional.of(Map.entry("list-type", "2")),
                    Optional.ofNullable(prefix)
                        .map(value -> Map.entry("prefix", value)),
                    Optional.ofNullable(continuationToken)
                        .map(value -> Map.entry("continuation-token", value))
                )
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)),
            null,
            null
        );
    }

    private HttpRequest putObjectRequest(String name, byte[] bytes) {
        return buildRequest("PUT", name, null, null, bytes);
    }

    private HttpRequest deleteObjectRequest(Collection<String> objects) {
        return buildRequest(
            "POST",
            "",
            Map.of("delete", ""),
            null,
            deletes(objects).getBytes(StandardCharsets.UTF_8)
        );
    }

    private HttpRequest getObjectRequest(String name, Range range) {
        return buildRequest("GET", name, null, range, null);
    }

    private HttpRequest buildRequest(
        String method,
        String name,
        Map<String, String> queryPars,
        Range range,
        byte[] body
    ) {
        try {
            var endpointUri = URI.create(endpoint(name) + qp(queryPars));
            var signer = new AwsAuthHeaderSigner(endpointUri, method, "s3", region);
            Map<String, String> headers = new HashMap<>();
            if (sessionToken != null) {
                headers.put("x-amz-security-token", sessionToken);
            }
            String bodyHash;
            if (body == null || body.length == 0) {
                headers.put("x-amz-content-sha256", bodyHash = EMPTY_BODY_SHA256);
            } else {
                headers.put("content-length", String.valueOf(body.length));
                headers.put("x-amz-content-sha256", bodyHash = hash(body));
            }
            if (range != null) {
                headers.put("Range", range.requestHeader());
            }
            if (method.equals("POST")) {
                headers.put("Content-MD5", md5(body));
            }
            var authorization =
                signer.computeSignature(time.instant(), headers, queryPars, bodyHash, accessKey, secretKey);
            headers.remove("content-length");
            var requestBuilder = HttpRequest.newBuilder(endpointUri)
                .method(
                    method,
                    body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofByteArray(body)
                )
                .header("Authorization", authorization)
                .version(HttpClient.Version.HTTP_1_1);
            headers.forEach(requestBuilder::header);
            return requestBuilder.build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build " + method + " request for " + name, e);
        }
    }

    private String endpoint(String name) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + UriEncoding.encode(name, true);
    }

    private static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";

    private static final String EMPTY_BODY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private static final long MAX_PRESIGN_SECONDS = Duration.ofDays(7).toSeconds();

    private static final int MAX_DELETE_KEYS = 1000;

    private static final int MAX_RETRIES = 3;

    private static final long BACKOFF_BASE_MILLIS = 100L;

    private static final Set<Integer> RETRYABLE_STATUSES = Set.of(500, 502, 503, 504);

    private static void backoff(HttpRequest request, int attempt) {
        try {
            Thread.sleep(BACKOFF_BASE_MILLIS << attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying " + request, e);
        }
    }

    private static <T> HttpResponse.BodyHandler<Outcome<T>> outcomeHandler(HttpResponse.BodyHandler<T> bodyHandler) {
        return info -> {
            var status = info.statusCode();
            if (status < 300) {
                return HttpResponse.BodySubscribers.mapping(
                    bodyHandler.apply(info),
                    Outcome.Ok::new
                );
            }
            if (status == 404) {
                return HttpResponse.BodySubscribers.replacing(new Outcome.NotFound<>());
            }
            return HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8),
                body -> new Outcome.Failed<>(status, body)
            );
        };
    }

    private static ListPage parseListPage(String xml) {
        return new ListPage(
            Xml.elements(xml, "Contents")
                .map(DefaultS3Accessor::parse)
                .toList(),
            Xml.text(xml, "IsTruncated")
                .filter(Boolean::parseBoolean)
                .flatMap(_ -> Xml.text(xml, "NextContinuationToken"))
        );
    }

    private static RemoteInfo parse(String contents) {
        return new RemoteInfo(
            field(contents, "Key"),
            ZonedDateTime.parse(field(contents, "LastModified"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .toInstant(),
            Long.parseLong(field(contents, "Size"))
        );
    }

    private static Stream<String> deleteErrors(String xml) {
        return Xml.elements(xml, "Error")
            .map(error ->
                Xml.text(error, "Key").orElse("?") + ": " +
                Xml.text(error, "Code").orElse("?") + " " +
                Xml.text(error, "Message").orElse(""));
    }

    private static String field(String xml, String tag) {
        return Xml.text(xml, tag)
            .orElseThrow(() -> new IllegalStateException("No <" + tag + "> in " + xml));
    }

    @SuppressWarnings("HttpUrlsUsage")
    private static String deletes(Collection<String> objects) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <Delete xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
            %s  <Quiet>true</Quiet>
            </Delete>
            """.formatted(
            objects.stream()
                .map(object -> "  <Object><Key>" + Xml.escape(object) + "</Key></Object>\n")
                .collect(Collectors.joining())
        );
    }

    private static String hash(byte[] body) {
        return HexFormat.of().formatHex(sha256(body));
    }

    private static String qp(Map<String, String> qps) {
        if (qps == null || qps.isEmpty()) {
            return "";
        }
        return "?" + qps.entrySet()
            .stream()
            .map(e ->
                UriEncoding.encode(e.getKey(), false) +
                (e.getValue().isEmpty() ? "" : "=" + UriEncoding.encode(e.getValue(), false)))
            .collect(Collectors.joining("&"));
    }

    private record ListPage(List<RemoteInfo> remoteInfos, Optional<String> continuationToken) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               Print.semiSecret(accessKey) + " -> " + bucket + "@" + region +
               "]";
    }

    private sealed interface Outcome<T> {

        record Ok<T>(T body) implements Outcome<T> {
        }

        record NotFound<T>() implements Outcome<T> {
        }

        record Failed<T>(int status, String body) implements Outcome<T> {
        }
    }
}
