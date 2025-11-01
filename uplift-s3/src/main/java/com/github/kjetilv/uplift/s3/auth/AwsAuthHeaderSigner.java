package com.github.kjetilv.uplift.s3.auth;

import com.github.kjetilv.uplift.s3.util.BinaryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/// Common methods and properties for all AWS4 signer variants
public final class AwsAuthHeaderSigner extends AbstractSigner {

    private static final Logger log = LoggerFactory.getLogger(AwsAuthHeaderSigner.class);

    /// Create a new AWS V4 signer.
    ///
    /// @param endpointUrl The service endpoint, including the path to any resource.
    /// @param httpMethod  The HTTP verb for the request, e.g. GET.
    /// @param serviceName The signing name of the service, e.g. 's3'.
    /// @param regionName  The system name of the AWS region associated with the
    ///                                       endpoint, e.g. us-east-1.
    public AwsAuthHeaderSigner(
        URI endpointUrl,
        String httpMethod,
        String serviceName,
        String regionName
    ) {
        super(endpointUrl, httpMethod, serviceName, regionName);
    }

    /// Computes an AWS4 signature for a request, ready for inclusion as an
    /// 'Authorization' header.
    ///
    /// @param headers         The request headers; 'Host' and 'X-Amz-Date' will be added to
    ///                        this set.
    /// @param queryParameters Any query parameters that will be added to the endpoint. The
    ///                        parameters should be specified in canonical format.
    /// @param bodyHash        Precomputed SHA256 hash of the request body content; this
    ///                        value should also be set as the header 'X-Amz-Content-SHA256'
    ///                        for non-streaming uploads.
    /// @param awsAccessKey    The user's AWS Access Key.
    /// @param awsSecretKey    The user's AWS Secret Key.
    /// @return The computed authorization string for the request. This value
    /// needs to be set as the header 'Authorization' on the subsequent
    /// HTTP request.
    public String computeSignature(
        Map<String, String> headers,
        Map<String, String> queryParameters,
        String bodyHash,
        String awsAccessKey,
        String awsSecretKey
    ) {
        // first get the date and time for the subsequent request, and convert
        // to ISO 8601 format for use in signature generation
        var now = now();
        var dateTimeStamp = formatDateTime(now);

        // update the headers with required 'x-amz-date' and 'host' values
        headers.put("x-amz-date", dateTimeStamp);

        var port = getEndpointUrl().getPort();
        var hostHeader = port > -1
            ? getEndpointUrl().getHost() + ":" + port
            : getEndpointUrl().getHost();
        headers.put("Host", hostHeader);

        // canonicalize the headers; we need the set of header names as well as the
        // names and values to go into the signature process
        var canonicalizedHeaderNames = AwsAuths.getCanonicalizeHeaderNames(headers);
        var canonicalizedHeaders = AwsAuths.getCanonicalizedHeaderString(headers);
        // if any query string parameters have been supplied, canonicalize them
        var canonicalizedQueryParameters = AwsAuths.getCanonicalizedQueryString(queryParameters);

        // canonicalize the various components of the request
        var canonicalRequest = AwsAuths.getCanonicalRequest(
            getEndpointUrl(),
            getHttpMethod(),
            canonicalizedQueryParameters,
            canonicalizedHeaderNames,
            canonicalizedHeaders,
            bodyHash
        );
        log.trace("--------- Canonical request --------");
        log.trace(canonicalRequest);
        log.trace("------------------------------------");

        // construct the string to be signed
        var dateStamp = formatDatestamp(now);
        var scope = dateStamp + "/" + getRegionName() + "/" + getServiceName() + "/" + AwsAuths.TERMINATOR;
        var stringToSign = AwsAuths.getStringToSign(dateTimeStamp, scope, canonicalRequest);

        log.trace("--------- String to sign -----------");
        log.trace(stringToSign);
        log.trace("------------------------------------");

        // compute the signing key
        var kSecret = (AwsAuths.SCHEME + awsSecretKey).getBytes(StandardCharsets.UTF_8);
        var kDate = AwsAuths.sign(dateStamp, kSecret);
        var kRegion = AwsAuths.sign(getRegionName(), kDate);
        var kService = AwsAuths.sign(getServiceName(), kRegion);
        var kSigning = AwsAuths.sign(AwsAuths.TERMINATOR, kService);
        var signature = AwsAuths.sign(stringToSign, kSigning);

        var credentialsAuthorizationHeader = "Credential=" + awsAccessKey + "/" + scope;
        var signedHeadersAuthorizationHeader = "SignedHeaders=" + canonicalizedHeaderNames;
        var signatureAuthorizationHeader = "Signature=" + BinaryUtils.toHex(signature);

        var authorizationHeader = AwsAuths.SCHEME + "-" + AwsAuths.ALGORITHM + " "
                                  + credentialsAuthorizationHeader + ", "
                                  + signedHeadersAuthorizationHeader + ", "
                                  + signatureAuthorizationHeader;
        headers.remove("Host");
        return authorizationHeader;
    }
}
