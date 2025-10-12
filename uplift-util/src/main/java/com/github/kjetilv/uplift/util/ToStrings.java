package com.github.kjetilv.uplift.util;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class ToStrings {

    private ToStrings() {
    }

    public static void print(StringBuilder base, Map<String, List<String>> headers) {
        base.append(" headers:[");
        var count = 0;
        for (var entry: headers.entrySet()) {
            if (count > 0) {
                base.append(" ");
            }
            var key = entry.getKey();
            var value = entry.getValue();
            var size = value.size();
            if (size > 1) {
                base.append(key).append("=");
                base.append("[");
                for (var i = 0; i < size; i++) {
                    if (i > 0) {
                        base.append(" ");
                    }
                    base.append(value.getFirst());
                }
                base.append("]");
            } else if (size > 0) {
                base.append(key).append("=");
                base.append(value.getFirst());
            }
            count++;
        }
        base.append("]");
    }

    public static void print(StringBuilder base, byte[] body) {
        var length = body.length;
        if (length > 50) {
            base.append(" body:").append(length);
        } else {
            base.append('`').append(new String(body, UTF_8)).append('`');
        }
    }
}
