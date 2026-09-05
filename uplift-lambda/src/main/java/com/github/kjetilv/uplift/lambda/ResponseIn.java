package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.json.anno.JsonRecord;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@JsonRecord
public record ResponseIn(
    int statusCode,
    Map<String, Object> headers,
    String body,
    boolean isBase64Encoded,
    String reqId
) implements Headered {

    public byte[] bytes() {
        if (body == null || body.isEmpty()) {
            return EMPTY_BODY;
        }
        return isBase64Encoded
            ? Base64.getDecoder().decode(body)
            : body.getBytes(UTF_8);
    }

    public Status status() {
        return statusCode >= 500 ? Status.PROCESSING_ERROR
            : statusCode >= 400 ? Status.BAD_REQUEST
                : Status.OK;
    }

    Optional<String> range() {
        return header("content-range");
    }

    private static final byte[] EMPTY_BODY = new byte[0];

    private static final String BYTES = "bytes ";

    private static final int BYTES_LENGTH = BYTES.length();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               (reqId == null ? "<no id>" : "[" + reqId + "]") +
               " " + statusCode +
               range()
                   .map(range -> range.startsWith(BYTES)
                       ? range.substring(BYTES_LENGTH)
                       : range)
                   .map(range -> " r:" + range).orElse("") +
               " h:" + Utils.headers(headers) +
               " b:" + Utils.printBody(
            body,
            isBase64Encoded ? "base64" : null,
            20,
            30
        ) + "]";
    }

    public enum Status {
        OK,
        BAD_REQUEST,
        PROCESSING_ERROR
    }
}
