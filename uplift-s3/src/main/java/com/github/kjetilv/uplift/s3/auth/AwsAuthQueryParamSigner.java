package com.github.kjetilv.uplift.s3.auth;

import module java.base;
import com.github.kjetilv.uplift.s3.util.BinaryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.kjetilv.uplift.s3.auth.AwsAuths.ALGORITHM;
import static com.github.kjetilv.uplift.s3.auth.AwsAuths.SCHEME;

/// Sample AWS4 signer demonstrating how to sign requests to Amazon S3 using
/// query string parameters.
public final class AwsAuthQueryParamSigner extends AbstractSigner {

    private static final Logger log = LoggerFactory.getLogger(AwsAuthQueryParamSigner.class);

    /// Create a new AWS V4 signer.
    ///
    /// @param endpointUrl The service endpoint, including the path to any resource.
    /// @param httpMethod  The HTTP verb for the request, e.g. GET.
    /// @param serviceName The signing name of the service, e.g. 's3'.
    /// @param regionName  The system name of the AWS region associated with the
    ///                                                                             endpoint, e.g. us-east-1.
    public AwsAuthQueryParamSigner(
        URI endpointUrl,
        String httpMethod,
        String serviceName,
        String regionName
    ) {
        super(endpointUrl, httpMethod, serviceName, regionName);
    }

    /// Computes an AWS4 authorization for a request, suitable for embedding in
    /// query parameters.
    ///
    /// @param headers         The request headers; 'Host' and 'X-Amz-Date' will be added to
    ///                                                                                             this set.
    /// @param queryParameters Any query parameters that will be added to the endpoint. The
    ///                                                                                             parameters should be specified in canonical format.
    /// @param bodyHash        Precomputed SHA256 hash of the request body content; this
    ///                                                                                             value should also be set as the header 'X-Amz-Content-SHA256'
    ///                                                                                             for non-streaming uploads.
    /// @param awsAccessKey    The user's AWS Access Key.
    /// @param awsSecretKey    The user's AWS Secret Key.
    /// @return The computed authorization string for the request. This value
    ///     needs to be set as the header 'Authorization' on the subsequent
    ///     HTTP request.
    public String computeSignature(
        Map<String, String> headers,
        Map<String, String> queryParameters,
        String bodyHash,
        String awsAccessKey,
        String awsSecretKey
    ) {
        // first get the date and time for the subsequent request, and convert
        // to ISO 8601 format
        // for use in signature generation
        var now = now();
        var dateTimeStamp = formatDateTime(now);

        // make sure "Host" header is added
        var hostHeader = getEndpointUrl().getPort() > -1
            ? "%s:%d".formatted(getEndpointUrl().getHost(), getEndpointUrl().getPort())
            : getEndpointUrl().getHost();
        headers.put("Host", hostHeader);

        // canonicalized headers need to be expressed in the query
        // parameters processed in the signature
        var canonicalizedHeaderNames = AwsAuths.getCanonicalizeHeaderNames(headers);
        var canonicalizedHeaders = AwsAuths.getCanonicalizedHeaderString(headers);

        // we need scope as part of the query parameters
        var dateStamp = formatDatestamp(now);
        var scope = dateStamp + "/" + getRegionName() + "/" + getServiceName() + "/" + AwsAuths.TERMINATOR;

        // add the fixed authorization params required by Signature V4
        queryParameters.put("X-Amz-Algorithm", SCHEME + "-" + ALGORITHM);
        queryParameters.put("X-Amz-Credential", awsAccessKey + "/" + scope);

        // x-amz-date is now added as a query parameter, but still need to be in ISO8601 basic form
        queryParameters.put("X-Amz-Date", dateTimeStamp);

        queryParameters.put("X-Amz-SignedHeaders", canonicalizedHeaderNames);

        // build the expanded canonical query parameter string that will go into the
        // signature computation
        var canonicalizedQueryParameters = AwsAuths.getCanonicalizedQueryString(queryParameters);

        // express all the header and query parameter data as a canonical request string
        var endpoint = getEndpointUrl();
        var httpMethod = getHttpMethod();
        var canonicalRequest = AwsAuths.getCanonicalRequest(
            endpoint,
            httpMethod,
            canonicalizedQueryParameters,
            canonicalizedHeaderNames,
            canonicalizedHeaders,
            bodyHash
        );
        log.trace("--------- Canonical request --------");
        log.trace(canonicalRequest);
        log.trace("------------------------------------");

        // construct the string to be signed
        var stringToSign = AwsAuths.getStringToSign(dateTimeStamp, scope, canonicalRequest);
        log.trace("--------- String to sign -----------");
        log.trace(stringToSign);
        log.trace("------------------------------------");

        // compute the signing key
        var kSecret = (SCHEME + awsSecretKey).getBytes(StandardCharsets.UTF_8);
        var kDate = AwsAuths.sign(dateStamp, kSecret);
        var stringData1 = getRegionName();
        var kRegion = AwsAuths.sign(stringData1, kDate);
        var stringData = getServiceName();
        var kService = AwsAuths.sign(stringData, kRegion);
        var kSigning = AwsAuths.sign(AwsAuths.TERMINATOR, kService);
        var signature = AwsAuths.sign(stringToSign, kSigning);

        // form up the authorization parameters for the caller to place in the query string

        return "X-Amz-Algorithm=" + queryParameters.get("X-Amz-Algorithm") +
               "&X-Amz-Credential=" + queryParameters.get("X-Amz-Credential") +
               "&X-Amz-Date=" + queryParameters.get("X-Amz-Date") +
               "&X-Amz-Expires=" + queryParameters.get("X-Amz-Expires") +
               "&X-Amz-SignedHeaders=" + queryParameters.get("X-Amz-SignedHeaders") +
               "&X-Amz-Signature=" + BinaryUtils.toHex(signature);
    }
}
