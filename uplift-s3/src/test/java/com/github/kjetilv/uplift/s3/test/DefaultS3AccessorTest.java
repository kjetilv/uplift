package com.github.kjetilv.uplift.s3.test;

import module java.base;
import module java.net.http;
import com.github.kjetilv.uplift.kernel.Env;
import com.github.kjetilv.uplift.kernel.io.BytesIO;
import com.github.kjetilv.uplift.s3.DefaultS3Accessor;
import com.github.kjetilv.uplift.s3.S3Accessor;
import com.github.kjetilv.uplift.s3.test.FakeHttpClient.Reply;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultS3AccessorTest {

    @Test
    void listWithoutPrefixFollowsContinuationTokens() {
        var client = new FakeHttpClient(exchange ->
            new Reply(
                200,
                exchange.request().uri().getRawQuery().contains("continuation-token=next%2Fpage")
                    ? listing(false, null, contents("b&amp;c.txt", 2))
                    : listing(true, "next/page", contents("a.txt", 1))
            ));

        var infos = accessor(client).remoteInfos();

        assertThat(infos).containsOnlyKeys("a.txt", "b&c.txt");
        assertThat(infos.get("b&c.txt").size()).isEqualTo(2L);
        assertThat(client.exchanges()).hasSize(2);
        assertThat(client.exchanges().getFirst().request().uri().getRawQuery()).doesNotContain("prefix");
    }

    @Test
    void remoteInfoMatchesExactKey() {
        var client = new FakeHttpClient(_ ->
            new Reply(200, listing(false, null, contents("a.txt", 1) + contents("a.txt.bak", 2))));
        var accessor = accessor(client);

        assertThat(accessor.remoteInfo("a.txt").map(S3Accessor.RemoteInfo::size)).hasValue(1L);
        assertThat(accessor.remoteInfo("a.tx")).isEmpty();
    }

    @Test
    void keyAndPrefixAreEncoded() {
        var client = new FakeHttpClient(_ -> new Reply(200, listing(false, null, "")));
        var accessor = accessor(client);

        accessor.stream("dir/my file*.txt");
        accessor.remoteInfos("a b");

        assertThat(client.exchanges().get(0).request().uri().getRawPath()).isEqualTo("/dir/my%20file%2A.txt");
        assertThat(client.exchanges().get(1).request().uri().getRawQuery()).contains("prefix=a%20b");
    }

    @Test
    void streamNotFoundIsEmpty() {
        var accessor = accessor(new FakeHttpClient(_ -> new Reply(404, "<Error>NoSuchKey</Error>")));

        assertThat(accessor.stream("missing")).isEmpty();
    }

    @Test
    void putToMissingBucketFails() {
        var accessor = accessor(new FakeHttpClient(_ -> new Reply(404, "<Error>NoSuchBucket</Error>")));

        assertThatThrownBy(() -> accessor.put("x", "y"))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void putStreamChecksLength() {
        var client = new FakeHttpClient(_ -> new Reply(200, ""));
        var accessor = accessor(client);

        assertThatThrownBy(() -> accessor.put("x", new ByteArrayInputStream(new byte[3]), 4))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(client.exchanges()).isEmpty();

        accessor.put("x", new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)), 3);
        accessor.put("y", new ByteArrayInputStream("de".getBytes(StandardCharsets.UTF_8)), -1);

        assertThat(client.exchanges())
            .extracting(FakeHttpClient.Exchange::body)
            .containsExactly("abc", "de");
    }

    @Test
    void putSendsBytesAndNoStorageClass() {
        var client = new FakeHttpClient(_ -> new Reply(200, ""));
        var accessor = accessor(client);

        accessor.put("x", "abc".getBytes(StandardCharsets.UTF_8));

        var exchange = client.exchanges().getFirst();
        assertThat(exchange.body()).isEqualTo("abc");
        assertThat(exchange.request().method()).isEqualTo("PUT");
        assertThat(exchange.request().headers().firstValue("x-amz-storage-class")).isEmpty();
        assertThat(exchange.request().headers().firstValue("x-amz-security-token")).hasValue("token");
    }

    @Test
    void removeBatchesAndEscapesKeys() {
        var client = new FakeHttpClient(_ -> new Reply(200, "<DeleteResult></DeleteResult>"));
        var keys = IntStream.range(0, 2500)
            .mapToObj(i -> "k&<" + i)
            .toList();

        accessor(client).remove(keys);

        assertThat(client.exchanges()).hasSize(3);
        assertThat(client.exchanges())
            .allSatisfy(exchange -> {
                assertThat(exchange.request().method()).isEqualTo("POST");
                assertThat(exchange.request().headers().firstValue("Content-MD5")).isPresent();
                assertThat(exchange.body()).doesNotContain("k&<");
            });
        assertThat(client.exchanges().getFirst().body()).contains("<Key>k&amp;&lt;0</Key>");
        assertThat(client.exchanges().getLast().body()).contains("<Key>k&amp;&lt;2499</Key>");
    }

    @Test
    void removeNothingSendsNothing() {
        var client = new FakeHttpClient(_ -> new Reply(200, ""));

        accessor(client).remove(List.of());

        assertThat(client.exchanges()).isEmpty();
    }

    @Test
    void removeReportsPerKeyErrors() {
        var accessor = accessor(new FakeHttpClient(_ ->
            new Reply(
                200,
                "<DeleteResult><Error><Key>a</Key><Code>AccessDenied</Code><Message>No</Message></Error></DeleteResult>"
            )));

        assertThatThrownBy(() -> accessor.remove("a", "b"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("a: AccessDenied");
    }

    @Test
    void retriesServerErrorsAndIoErrors() {
        var calls = new AtomicInteger();
        var client = new FakeHttpClient(_ ->
            switch (calls.getAndIncrement()) {
                case 0 -> throw new IOException("HTTP/1.1 header parser received no bytes");
                case 1 -> new Reply(503, "<Error>SlowDown</Error>");
                default -> new Reply(200, "data");
            });

        var data = accessor(client).stream("x").map(BytesIO::readUTF8);

        assertThat(data).hasValue("data");
        assertThat(client.exchanges()).hasSize(3);
    }

    @Test
    void givesUpAfterRetries() {
        var client = new FakeHttpClient(_ -> new Reply(500, "<Error>InternalError</Error>"));

        assertThatThrownBy(() -> accessor(client).stream("x"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("500");
        assertThat(client.exchanges()).hasSize(4);
    }

    @Test
    void doesNotRetryClientErrors() {
        var client = new FakeHttpClient(_ -> new Reply(403, "<Error>AccessDenied</Error>"));

        assertThatThrownBy(() -> accessor(client).stream("x"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("AccessDenied");
        assertThat(client.exchanges()).hasSize(1);
    }

    @Test
    void presignIncludesSessionToken() {
        var uri = accessor(new FakeHttpClient(_ -> new Reply(200, "")))
            .presign("my file.txt", Duration.ofHours(1))
            .orElseThrow();

        assertThat(uri.getHost()).isEqualTo("bucket.s3.eu-north-1.amazonaws.com");
        assertThat(uri.getRawPath()).isEqualTo("/my%20file.txt");
        assertThat(uri.getRawQuery())
            .contains("X-Amz-Security-Token=token")
            .contains("X-Amz-Expires=3600")
            .contains("X-Amz-Credential=AKID%2F20240101%2Feu-north-1%2Fs3%2Faws4_request")
            .containsPattern("X-Amz-Signature=[0-9a-f]{64}$");
    }

    @Test
    void presignRejectsLongLifetime() {
        var accessor = accessor(new FakeHttpClient(_ -> new Reply(200, "")));

        assertThatThrownBy(() -> accessor.presign("x", Duration.ofDays(8)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static DefaultS3Accessor accessor(HttpClient client) {
        return new DefaultS3Accessor(
            new TestEnv(),
            client,
            "bucket",
            null,
            () -> Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    @SuppressWarnings("HttpUrlsUsage")
    private static String listing(boolean truncated, String token, String contents) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <ListBucketResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
              <IsTruncated>%s</IsTruncated>
              %s
              %s
            </ListBucketResult>
            """.formatted(
            truncated,
            contents,
            token == null ? "" : "<NextContinuationToken>" + token + "</NextContinuationToken>"
        );
    }

    private static String contents(String key, long size) {
        return """
            <Contents>
              <Key>%s</Key>
              <LastModified>2024-01-01T00:00:00.000Z</LastModified>
              <ETag>"x"</ETag>
              <Size>%d</Size>
              <StorageClass>STANDARD</StorageClass>
            </Contents>
            """.formatted(key, size);
    }

    private record TestEnv() implements Env {

        @Override
        public URI awsLambdaUri() {
            return URI.create("http://localhost");
        }

        @Override
        public String awsLambdaRuntimeApi() {
            return "localhost";
        }

        @Override
        public String accessKey(String profile) {
            return "AKID";
        }

        @Override
        public String secretKey(String profile) {
            return "secret";
        }

        @Override
        public String sessionToken() {
            return "token";
        }
    }
}
