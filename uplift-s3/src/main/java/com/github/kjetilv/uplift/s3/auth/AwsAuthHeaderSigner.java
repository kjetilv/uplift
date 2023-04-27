package com.github.kjetilv.uplift.s3.auth;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Map;

import com.github.kjetilv.uplift.s3.util.BinaryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common methods and properties for all AWS4 signer variants
 */
public final class AwsAuthHeaderSigner extends AbstractSigner {

    private static final Logger log = LoggerFactory.getLogger(AwsAuthHeaderSigner.class);

    /**
     * Create a new AWS V4 signer.
     *
     * @param endpointUrl The service endpoint, including the path to any resource.
     * @param httpMethod  The HTTP verb for the request, e.g. GET.
     * @param serviceName The signing name of the service, e.g. 's3'.
     * @param regionName  The system name of the AWS region associated with the
     *                    endpoint, e.g. us-east-1.
     */
    public AwsAuthHeaderSigner(
        URI endpointUrl,
        String httpMethod,
        String serviceName,
        String regionName
    ) {
        super(endpointUrl, httpMethod, serviceName, regionName);
    }

    /**
     * Computes an AWS4 signature for a request, ready for inclusion as an
     * 'Authorization' header.
     *
     * @param headers         The request headers; 'Host' and 'X-Amz-Date' will be added to
     *                        this set.
     * @param queryParameters Any query parameters that will be added to the endpoint. The
     *                        parameters should be specified in canonical format.
     * @param bodyHash        Precomputed SHA256 hash of the request body content; this
     *                        value should also be set as the header 'X-Amz-Content-SHA256'
     *                        for non-streaming uploads.
     * @param awsAccessKey    The user's AWS Access Key.
     * @param awsSecretKey    The user's AWS Secret Key.
     *
     * @return The computed authorization string for the request. This value
     *     needs to be set as the header 'Authorization' on the subsequent
     *     HTTP request.
     */
    public String computeSignature(
        Map<String, String> headers,
        Map<String, String> queryParameters,
        String bodyHash,
        String awsAccessKey,
        String awsSecretKey
    ) {
        // first get the date and time for the subsequent request, and convert
        // to ISO 8601 format for use in signature generation
        ZonedDateTime now = now();
        String dateTimeStamp = formatDateTime(now);

        // update the headers with required 'x-amz-date' and 'host' values
        headers.put("x-amz-date", dateTimeStamp);

        int port = getEndpointUrl().getPort();
        String hostHeader = port > -1
            ? getEndpointUrl().getHost() + ":" + port
            : getEndpointUrl().getHost();
        headers.put("Host", hostHeader);

        // canonicalize the headers; we need the set of header names as well as the
        // names and values to go into the signature process
        String canonicalizedHeaderNames = AwsAuths.getCanonicalizeHeaderNames(headers);
        String canonicalizedHeaders = AwsAuths.getCanonicalizedHeaderString(headers);
        // if any query string parameters have been supplied, canonicalize them
        String canonicalizedQueryParameters = AwsAuths.getCanonicalizedQueryString(queryParameters);

        // canonicalize the various components of the request
        String canonicalRequest = AwsAuths.getCanonicalRequest(
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
        String dateStamp = formatDatestamp(now);
        String scope = dateStamp + "/" + getRegionName() + "/" + getServiceName() + "/" + AwsAuths.TERMINATOR;
        String stringToSign = AwsAuths.getStringToSign(dateTimeStamp, scope, canonicalRequest);

        log.trace("--------- String to sign -----------");
        log.trace(stringToSign);
        log.trace("------------------------------------");

        // compute the signing key
        byte[] kSecret = (AwsAuths.SCHEME + awsSecretKey).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = AwsAuths.sign(dateStamp, kSecret);
        byte[] kRegion = AwsAuths.sign(getRegionName(), kDate);
        byte[] kService = AwsAuths.sign(getServiceName(), kRegion);
        byte[] kSigning = AwsAuths.sign(AwsAuths.TERMINATOR, kService);
        byte[] signature = AwsAuths.sign(stringToSign, kSigning);

        String credentialsAuthorizationHeader = "Credential=" + awsAccessKey + "/" + scope;
        String signedHeadersAuthorizationHeader = "SignedHeaders=" + canonicalizedHeaderNames;
        String signatureAuthorizationHeader = "Signature=" + BinaryUtils.toHex(signature);

        String authorizationHeader = AwsAuths.SCHEME + "-" + AwsAuths.ALGORITHM + " "
                                     + credentialsAuthorizationHeader + ", "
                                     + signedHeadersAuthorizationHeader + ", "
                                     + signatureAuthorizationHeader;
        headers.remove("Host");
        return authorizationHeader;
    }
}
