package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@JsonRecord
public record ResponseIn(
    int statusCode,
    Map<String, Object> headers,
    String body,
    boolean isBase64Encoded,
    String reqId
) {

    public static ResponseIn read(InputStream bytes) {
        return ResponseInRW.INSTANCE.streamReader().read(bytes);
    }

    public long bodyLength() {
        return body == null ? 0 : body.length();
    }

    public byte[] bytes() {
        if (body == null || body.isEmpty()) {
            return EMPTY_BODY;
        }
        return isBase64Encoded
            ? Base64.getDecoder().decode(body)
            : body.getBytes(UTF_8);
    }

    private static final byte[] EMPTY_BODY = new byte[0];

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[[" + reqId + "] " + statusCode +
               " h:" + Utils.headers(headers) +
               " b:" + Utils.printBody(body) +
               (isBase64Encoded ? " base64" : "") +
               "]";
    }
}
