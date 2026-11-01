package com.github.kjetilv.uplift.s3.test;

import module java.base;
import com.github.kjetilv.uplift.s3.auth.AwsAuthHeaderSigner;
import com.github.kjetilv.uplift.s3.auth.AwsAuthQueryParamSigner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/// Example requests and signatures from the Amazon S3 API reference,
/// "Signature Calculations for the Authorization Header" and "Authenticating Requests: Using Query Parameters".
/// The PUT case is the exception, see its comment.
class SigV4Test {

    @Test
    void getObject() {
        var headers = new HashMap<String, String>();
        headers.put("Range", "bytes=0-9");
        headers.put("x-amz-content-sha256", EMPTY_SHA256);

        var authorization = headerSigner("GET", "https://examplebucket.s3.amazonaws.com/test.txt")
            .computeSignature(TIME, headers, null, EMPTY_SHA256, ACCESS_KEY, SECRET_KEY);

        assertThat(authorization).isEqualTo(
            "AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request, " +
            "SignedHeaders=host;range;x-amz-content-sha256;x-amz-date, " +
            "Signature=f0e8bdb87c964420e857bd35b5d6ed310bd44f0170aba48dd91039c6036bdb41"
        );
    }

    /// The expected signature was computed with an independent implementation (Python hmac/hashlib) of the
    /// canonical request from the AWS PUT Object example, with the key encoded as `/test%24file.text`.
    @Test
    void putObjectWithReservedCharacterInKey() {
        var bodyHash = "44ce7dd67c959e0d3524ffac1771dfbba87d2b6b4b4e99e42034a8b803f2b0e5";
        var headers = new HashMap<String, String>();
        headers.put("Date", "Fri, 24 May 2013 00:00:00 GMT");
        headers.put("x-amz-storage-class", "REDUCED_REDUNDANCY");
        headers.put("x-amz-content-sha256", bodyHash);

        var authorization = headerSigner("PUT", "https://examplebucket.s3.amazonaws.com/test$file.text")
            .computeSignature(TIME, headers, null, bodyHash, ACCESS_KEY, SECRET_KEY);

        assertThat(authorization).endsWith(
            "SignedHeaders=date;host;x-amz-content-sha256;x-amz-date;x-amz-storage-class, " +
            "Signature=52dde053f47f138d617d28ebd1c101b790d29e078bfb42b50627ebe50c5aed7f"
        );
    }

    @Test
    void listObjects() {
        var headers = new HashMap<String, String>();
        headers.put("x-amz-content-sha256", EMPTY_SHA256);

        var authorization = headerSigner("GET", "https://examplebucket.s3.amazonaws.com/?max-keys=2&prefix=J")
            .computeSignature(
                TIME,
                headers,
                Map.of("max-keys", "2", "prefix", "J"),
                EMPTY_SHA256,
                ACCESS_KEY,
                SECRET_KEY
            );

        assertThat(authorization).endsWith(
            "Signature=34b48302e7b5fa45bde8084f4b7868a86f0a534bc59db6670ed5711ef69dc6f7"
        );
    }

    @Test
    void presignedUrl() {
        var queryParameters = new HashMap<String, String>();
        queryParameters.put("X-Amz-Expires", "86400");

        var query = new AwsAuthQueryParamSigner(
            URI.create("https://examplebucket.s3.amazonaws.com/test.txt"), "GET", "s3", "us-east-1"
        ).computeSignature(TIME, new HashMap<>(), queryParameters, "UNSIGNED-PAYLOAD", ACCESS_KEY, SECRET_KEY);

        assertThat(query).isEqualTo(
            "X-Amz-Algorithm=AWS4-HMAC-SHA256" +
            "&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE%2F20130524%2Fus-east-1%2Fs3%2Faws4_request" +
            "&X-Amz-Date=20130524T000000Z" +
            "&X-Amz-Expires=86400" +
            "&X-Amz-SignedHeaders=host" +
            "&X-Amz-Signature=aeeed9bbccd4d02ee5c0109b86d86835f995330da4c265957d157751f604d404"
        );
    }

    private static final Instant TIME = Instant.parse("2013-05-24T00:00:00Z");

    private static final String ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";

    private static final String SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    private static final String EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private static AwsAuthHeaderSigner headerSigner(String method, String uri) {
        return new AwsAuthHeaderSigner(URI.create(uri), method, "s3", "us-east-1");
    }
}
