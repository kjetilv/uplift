package com.github.kjetilv.uplift.s3;

import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.io.BytesIO;
import com.github.kjetilv.uplift.util.Print;
import com.github.kjetilv.uplift.kernel.io.Range;
import com.github.kjetilv.uplift.util.Maps;
import com.github.kjetilv.uplift.s3.auth.AwsAuthHeaderSigner;
import com.github.kjetilv.uplift.s3.auth.AwsAuthQueryParamSigner;
import com.github.kjetilv.uplift.s3.util.BinaryUtils;
import com.github.kjetilv.uplift.s3.util.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    public DefaultS3Accessor(Env env, HttpClient httpClient, String bucket, String region) {
        requireNonNull(env, "env");
        this.httpClient = requireNonNull(httpClient, "httpClient");
        this.accessKey = requireNonNull(env.accessKey(), "Env.accessKey()");
        this.secretKey = requireNonNull(env.secretKey(), "Env.secretKey()");
        this.sessionToken = env.sessionToken();
        this.bucket = requireNonNull(bucket, "bucket");
        this.region = region == null || region.isBlank() ? "eu-north-1" : region;
    }

    @Override
    public Optional<? extends InputStream> stream(String name) {
        return streamFrom(getObjectRequest(name, null));
    }

    @Override
    public Optional<URI> presign(String name, Duration timeToLive) {

        URI endpointUrl = URI.create("https://s3-%s.amazonaws.com/%s/%s".formatted(region, bucket, name));

        // construct the query parameter string to accompany the url
        Map<String, String> queryParams = new HashMap<>();

        // for SignatureV4, the max expiry for a presigned url is 7 days,
        // expressed in seconds
        int expiresIn = Math.toIntExact(timeToLive.toSeconds());
        queryParams.put("X-Amz-Expires", String.valueOf(expiresIn));

        // we have no headers for this sample, but the signer will add 'host'
        Map<String, String> headers = new HashMap<>();

        AwsAuthQueryParamSigner signer = new AwsAuthQueryParamSigner(
            endpointUrl, "GET", "s3", region);
        String authorizationQueryParameters = signer.computeSignature(
            headers,
            queryParams,
            UNSIGNED_PAYLOAD,
            accessKey,
            secretKey
        );

        // build the presigned url to incorporate the authorization elements as query parameters
        String presignedUrl = endpointUrl + "?" + authorizationQueryParameters;
        log.trace("--------- Computed presigned url ---------");
        log.trace(presignedUrl);
        log.trace("------------------------------------------");

        return Optional.of(URI.create(presignedUrl));
    }

    @Override
    public Optional<? extends InputStream> stream(String name, Range range) {
        return streamFrom(getObjectRequest(name, range));
    }

    @Override
    public void put(String contents, String remoteName) {
        streamFrom(putObjectRequest(remoteName, contents.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void put(String remoteName, InputStream inputStream, long length) {
        streamFrom(putObjectRequest(remoteName, BytesIO.readInputStream(inputStream)))
            .ifPresent(close(remoteName));
    }

    @Override
    public Map<String, RemoteInfo> remoteInfos(String prefix) {
        return streamFrom(listObjectsRequest(prefix))
            .map(BytesIO::readUTF8)
            .map(xml ->
                Xml.objectList(xml, "Contents")
                    .map(DefaultS3Accessor::parse)
                    .toList())
            .map(remoteInfos ->
                Maps.indexBy(remoteInfos, RemoteInfo::key))
            .orElseGet(Collections::emptyMap);
    }

    @Override
    public void remove(Collection<String> objects) {
        streamFrom(deleteObjectRequest(objects));
    }

    private Optional<InputStream> streamFrom(HttpRequest request) {
        return streamFrom(request, 3, null);
    }

    @SuppressWarnings("MagicNumber")
    private Optional<InputStream> streamFrom(HttpRequest request, int retriesLeft, Exception failure) {
        HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 300) {
                return Optional.of(response.body());
            }
            if (response.statusCode() == 404) {
                return Optional.empty();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to query for " + request, e);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.startsWith("HTTP/1.1 header parser received no bytes") && retriesLeft > 0) {
                if (failure != null) {
                    e.addSuppressed(failure);
                }
                return streamFrom(request, retriesLeft - 1, e);
            } else {
                throw new IllegalStateException("Failed to query for " + request, e);
            }
        }
        throw new IllegalStateException(
            "Failed to get " + request + " => " + response + ": " + BytesIO.readUTF8(response.body()));
    }

    private HttpRequest listObjectsRequest(String prefix) {
        return buildRequest(
            HttpRequest.newBuilder().GET(),
            "GET",
            "",
            Map.of(
                "list-type", "2",
                "prefix", prefix
            ),
            null,
            null
        );
    }

    private HttpRequest putObjectRequest(String name, byte[] bytes) {
        return buildRequest(
            HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofByteArray(bytes)),
            "PUT",
            name,
            null,
            null,
            bytes
        );
    }

    private HttpRequest deleteObjectRequest(Collection<String> objects) {
        String deletes = deletes(objects);
        return buildRequest(
            HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(deletes)),
            "POST",
            "",
            Map.of("delete", ""),
            null,
            deletes.getBytes(StandardCharsets.UTF_8)
        );
    }

    private HttpRequest getObjectRequest(String name, Range range) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().GET();
        return buildRequest(
            requestBuilder,
            "GET",
            name,
            null,
            range,
            null
        );
    }

    private HttpRequest buildRequest(
        HttpRequest.Builder requestBuilder,
        String method,
        String name,
        Map<String, String> queryPars,
        Range range,
        byte[] body
    ) {
        try {
            URI endpointUri =
                URI.create("https://" + bucket + ".s3." + region + ".amazonaws.com/" + name + qp(queryPars));
            requestBuilder.uri(endpointUri);
            AwsAuthHeaderSigner signer = new AwsAuthHeaderSigner(endpointUri, method, "s3", region);
            Map<String, String> headers = new HashMap<>();
            if (sessionToken != null) {
                headers.put("x-amz-security-token", sessionToken);
            }
            String bodyHash;
            if (body == null || body.length == 0) {
                headers.put("x-amz-content-sha256", bodyHash = EMPTY_BODY_SHA256);
            } else {
                headers.put("content-length", String.valueOf(body.length));
                headers.put("x-amz-storage-class", "REDUCED_REDUNDANCY");
                headers.put("x-amz-content-sha256", bodyHash = hash(body));
            }
            if (range != null) {
                headers.put("Range", range.requestHeader());
            }
            if (method.equals("POST")) {
                headers.put("Content-MD5", md5(body));
            }
            String authorization =
                signer.computeSignature(headers, queryPars, bodyHash, accessKey, secretKey);
            headers.remove("content-length");
            headers.forEach(requestBuilder::header);
            requestBuilder.header("Authorization", authorization);
            return requestBuilder
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to access", e);
        }
    }

    private static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";

    private static final String EMPTY_BODY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private static RemoteInfo parse(String contents) {
        Map<String, String> fields = Maps.fromEntries(
            Xml.objectFields(contents, "Key", "LastModified", "Size")
        );
        return new RemoteInfo(
            fields.get("Key"),
            dateTime(fields.get("LastModified"))
                .toInstant(),
            Long.parseLong(fields.get("Size"))
        );
    }

    private static ZonedDateTime dateTime(CharSequence date) {
        return ZonedDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @SuppressWarnings("HttpUrlsUsage")
    private static String deletes(Collection<String> objects) {
        return String.format(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <Delete xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
               %s  <Quiet>true</Quiet>
            </Delete>
            """,
            objects.stream().map(object ->
                String.format(
                    """
                    <Object>
                        <Key>%s</Key>
                      </Object>
                    """,
                    object
                )).collect(Collectors.joining("  "))
        );
    }

    private static String hash(byte[] body) {
        return BinaryUtils.toHex(sha256(body));
    }

    private static String qp(Map<String, String> qps) {
        if (qps == null || qps.isEmpty()) {
            return "";
        }
        return "?" + qps.entrySet().stream()
            .map(e -> e.getKey() + (e.getValue().isEmpty() ? "" : "=" + e.getValue()))
            .collect(Collectors.joining("&"));
    }

    private static Consumer<InputStream> close(String remoteName) {
        return stream -> {
            try {
                stream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to close put to " + remoteName, e);
            }
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
            Print.semiSecret(accessKey) + "/*** -> " + bucket + "@" + region +
            "]";
    }
}
