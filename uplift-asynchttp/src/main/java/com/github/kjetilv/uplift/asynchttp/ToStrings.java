package com.github.kjetilv.uplift.asynchttp;

import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

final class ToStrings {

    private ToStrings() {
    }

    static void print(StringBuilder base, Map<String, List<String>> headers) {
        base.append(" headers:[");
        int count = 0;
        for (Map.Entry<String, List<String>> entry: headers.entrySet()) {
            if (count > 0) {
                base.append(" ");
            }
            String key = entry.getKey();
            List<String> value = entry.getValue();
            int size = value.size();
            if (size > 1) {
                base.append(key).append("=");
                base.append("[");
                for (int i = 0; i < size; i++) {
                    if (i > 0) {
                        base.append(" ");
                    }
                    base.append(value.get(0));
                }
                base.append("]");
            } else if (size > 0) {
                base.append(key).append("=");
                base.append(value.get(0));
            }
            count++;
        }
        base.append("]");
    }

    static void print(StringBuilder base, byte[] body) {
        int length = body.length;
        if (length > 50) {
            base.append(" body:").append(length);
        } else {
            base.append('`').append(new String(body, UTF_8)).append('`');
        }
    }
}
