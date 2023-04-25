package com.github.kjetilv.uplift.s3.auth;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.github.kjetilv.uplift.s3.util.BinaryUtils;

import static com.github.kjetilv.uplift.s3.auth.Hashes.sha256;

final class AwsAuths {

    static final String SCHEME = "AWS4";

    static final String ALGORITHM = "HMAC-SHA256";

    static final String TERMINATOR = "aws4_request";

    static final DateTimeFormatter DATETIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ROOT);

    static final DateTimeFormatter DATESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT);

    static final ZoneId UTC = ZoneId.of("UTC");

    static byte[] sign(String stringData, byte[] key) {
        try {
            byte[] data = stringData.getBytes(StandardCharsets.UTF_8);
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(key, HMAC_SHA_256));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("Unable to calculate a request signature: " + e.getMessage(), e);
        }
    }

    /**
     * Examines the specified query string parameters and returns a
     * canonicalized form.
     * <p>
     * The canonicalized query string is formed by first sorting all the query
     * string parameters, then URI encoding both the key and value and then
     * joining them, in order, separating key value pairs with an '&'.
     *
     * @param parameters The query string parameters to be canonicalized.
     *
     * @return A canonicalized form for the specified query string parameters.
     */
    static String getCanonicalizedQueryString(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }

        SortedMap<String, String> sorted = new TreeMap<>();

        for (Map.Entry<String, String> pair: parameters.entrySet()) {
            String key = pair.getKey();
            String value = pair.getValue();
            sorted.put(
                urlEncode(key, false),
                urlEncode(value, false)
            );
        }

        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<String, String>> entries = sorted.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> pair = entries.next();
            builder.append(pair.getKey());
            builder.append("=");
            builder.append(pair.getValue());
            if (entries.hasNext()) {
                builder.append("&");
            }
        }

        return builder.toString();
    }

    /**
     * Returns the canonical collection of header names that will be included in
     * the signature. For AWS4, all header names must be included in the process
     * in sorted canonicalized order.
     */
    static String getCanonicalizeHeaderNames(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        return headers.keySet().stream()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .map(s -> s.toLowerCase(Locale.ROOT))
            .collect(Collectors.joining(";"));
    }

    /**
     * Computes the canonical headers with values for the request. For AWS4, all
     * headers must be included in the signing process.
     */
    static String getCanonicalizedHeaderString(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }

        // step1: sort the headers by case-insensitive order
        List<String> sortedHeaders = new ArrayList<>(headers.keySet());
        sortedHeaders.sort(String.CASE_INSENSITIVE_ORDER);

        // step2: form the canonical header:value entries in sorted order.
        // Multiple white spaces in the values should be compressed to a single
        // space.
        StringBuilder buffer = new StringBuilder();
        for (String key: sortedHeaders) {
            buffer
                .append(WS.matcher(key.toLowerCase()).replaceAll(" "))
                .append(":")
                .append(WS.matcher(headers.get(key)).replaceAll(" "));
            buffer.append("\n");
        }
        return buffer.toString();
    }

    /**
     * Returns the canonical request string to go into the signer process; this
     * consists of several canonical sub-parts.
     *
     * @return Canonical request
     */
    static String getCanonicalRequest(
        URI endpoint,
        String httpMethod,
        String queryParameters,
        String canonicalizedHeaderNames,
        String canonicalizedHeaders,
        String bodyHash
    ) {
        return httpMethod + "\n" +
            getCanonicalizedResourcePath(endpoint) + "\n" +
            queryParameters + "\n" +
            canonicalizedHeaders + "\n" +
            canonicalizedHeaderNames + "\n" +
            bodyHash;
    }

    static String getStringToSign(
        String dateTime,
        String scope,
        String canonicalRequest
    ) {
        return SCHEME + "-" + ALGORITHM + "\n" +
            dateTime + "\n" +
            scope + "\n" +
            BinaryUtils.toHex(sha256(canonicalRequest));
    }

    private AwsAuths() {

    }

    private static final Pattern WS = Pattern.compile("\\s+");

    private static final String HMAC_SHA_256 = "HmacSHA256";

    private static String urlEncode(String url, boolean keepPathSlash) {
        String encoded = URLEncoder.encode(url, StandardCharsets.UTF_8);
        return keepPathSlash
            ? encoded.replace("%2F", "/")
            : encoded;
    }

    /**
     * Returns the canonicalized resource path for the service endpoint.
     */
    private static String getCanonicalizedResourcePath(URI endpoint) {
        if (endpoint == null) {
            return "/";
        }
        String path = endpoint.getPath();
        if (path == null || path.isEmpty()) {
            return "/";
        }

        String encodedPath = urlEncode(path, true);
        return encodedPath.startsWith("/")
            ? encodedPath
            : "/" + encodedPath;
    }
}
