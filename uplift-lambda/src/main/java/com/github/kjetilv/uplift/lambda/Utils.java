package com.github.kjetilv.uplift.lambda;

import com.github.kjetilv.uplift.kernel.io.BytesIO;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class Utils {

    public static String printQueryParams(Map<String, Object> params) {
        return params == null ? "{}"
            : params.size() > 3 ? "{" + params.size() + " qpars}"
                : params.toString();
    }

    static String printBody(byte[] body, boolean bin) {
        if (body == null || body.length == 0) {
            return "[]";
        }
        if (bin) {
            return "[" + body.length + " BASE64]";
        }
        if (body.length < MAX_PRINT) {
            return new String(body, UTF_8);
        }
        return new String(body, 0, CUTOFF_PRINT, UTF_8) + "... (" + body.length + " bytes)";
    }

    static String printBody(String body) {
        return printBody(body, null);
    }

    static String printBody(String body, String contents) {
        return printBody(body, contents, CUTOFF_PRINT, MAX_PRINT);
    }

    static String printBody(String body, String contents, int cutoffPrint, int maxPrint) {
        if (body == null || body.isBlank()) {
            return "[]";
        }
        if (body.length() < maxPrint) {
            return body;
        }
        return body.substring(0, cutoffPrint) + "⋯ (" + body.length() + " " +
               (contents == null ? "chars" : contents) + ")";
    }

    static String encodeResponseBody(byte[] body, boolean binary) {
        return binary ? BytesIO.toBase64(body) : toPlainString(body);
    }

    static String headers(Map<?, ?> headers) {
        return headers == null ? "{}"
            : headers.size() > 3 ? "{" + headers.size() + " headers}"
                : headers.toString();
    }

    private Utils() {
    }

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final int MAX_PRINT = 50;

    private static final int CUTOFF_PRINT = 25;

    private static String toPlainString(byte[] body) {
        return new String(body, UTF_8);
    }
}
