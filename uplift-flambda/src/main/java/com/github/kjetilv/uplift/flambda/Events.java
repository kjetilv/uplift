package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.lambda.RequestOut;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.rere.ReqHeader;

/// Builds API Gateway HTTP API events, payload format 2.0, from HTTP requests.
///
/// Header names are lower case, and duplicate headers and query parameters are joined with commas. Cookies are
/// moved from the headers to `cookies`. A body that is not valid UTF-8 is sent base64-encoded. `rawPath` and
/// `http.path` are not decoded.
final class Events {

    static RequestOut event(HttpReq httpReq, String requestId, Instant time, byte[] body) {
        var url = httpReq.path();
        var queryStart = url.indexOf('?');
        var rawPath = queryStart < 0 ? url : url.substring(0, queryStart);
        var rawQueryString = queryStart < 0 ? "" : url.substring(queryStart + 1);
        var headers = headers(httpReq);
        var cookies = cookies(httpReq);
        var query = httpReq.queryParametersMap();
        var method = httpReq.reqLine().method();
        var domainName = String.valueOf(headers.getOrDefault("host", "localhost"));
        var text = text(body);
        return new RequestOut(
            VERSION,
            ROUTE_KEY,
            null,
            null,
            rawPath,
            rawQueryString,
            cookies.isEmpty() ? null : cookies,
            headers,
            query.isEmpty() ? null : query,
            new RequestOut.RequestContext(
                ACCOUNT_ID,
                API_ID,
                domainName,
                domainName.split("\\.")[0],
                new RequestOut.RequestContext.Http(
                    method,
                    rawPath,
                    httpReq.reqLine().version(),
                    SOURCE_IP,
                    (String) headers.get("user-agent")
                ),
                requestId,
                ROUTE_KEY,
                STAGE,
                TIME_FORMAT.format(time),
                time.toEpochMilli()
            ),
            body.length > 0 && text == null,
            body.length == 0 ? null
                : text != null ? text
                    : Base64.getEncoder().encodeToString(body)
        );
    }

    private Events() {
    }

    private static final String VERSION = "2.0";

    private static final String ROUTE_KEY = "$default";

    private static final String STAGE = "$default";

    private static final String ACCOUNT_ID = "000000000000";

    private static final String API_ID = "flambda";

    /// The client address is not available from the server
    private static final String SOURCE_IP = "127.0.0.1";

    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    private static Map<String, Object> headers(HttpReq httpReq) {
        return httpReq.headers()
            .stream()
            .filter(header -> !header.is("cookie"))
            .collect(Collectors.toMap(
                header -> header.name().toLowerCase(Locale.ROOT),
                ReqHeader::value,
                (value1, value2) -> value1 + "," + value2,
                LinkedHashMap::new
            ));
    }

    private static List<String> cookies(HttpReq httpReq) {
        return httpReq.headers()
            .stream()
            .filter(header -> header.is("cookie"))
            .flatMap(header -> Arrays.stream(header.value().split(";")))
            .map(String::strip)
            .filter(cookie -> !cookie.isEmpty())
            .toList();
    }

    /// @return The body as text, or null if it is not valid UTF-8
    private static String text(byte[] body) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(body))
                .toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }
}
