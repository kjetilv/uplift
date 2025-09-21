package com.github.kjetilv.uplift.lambda;

import module java.base;
import module uplift.kernel;

final class Utils {

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
        if (body == null || body.isBlank()) {
            return "[]";
        }
        if (body.length() < MAX_PRINT) {
            return body;
        }
        return body.substring(0, CUTOFF_PRINT) + ".. (" + body.length() + " chars)";
    }

    static String encodeResponseBody(byte[] body, boolean binary) {
        return binary ? BytesIO.toBase64(body) : toPlainString(body);
    }

    private Utils() {
    }

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final int MAX_PRINT = 50;

    private static final int CUTOFF_PRINT = 40;

    private static String toPlainString(byte[] body) {
        return new String(body, UTF_8);
    }
}
