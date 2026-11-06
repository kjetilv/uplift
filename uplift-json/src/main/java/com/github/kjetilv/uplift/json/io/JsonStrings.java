package com.github.kjetilv.uplift.json.io;

/// Escaping of string content for JSON output.
public final class JsonStrings {

    /// Escapes `"`, `\` and all control characters below U+0020. The common control characters use their
    /// short forms (`\n`, `\t` and so on), the others a six-character unicode escape. Other characters are
    /// written as they are.
    ///
    /// @param value String content, without quotes
    /// @return Escaped content, the same instance if nothing needs escaping
    public static String escape(String value) {
        var length = value.length();
        var first = 0;
        while (first < length && !needsEscape(value.charAt(first))) {
            first++;
        }
        if (first == length) {
            return value;
        }
        var escaped = new StringBuilder(length + 16).append(value, 0, first);
        for (var i = first; i < length; i++) {
            var c = value.charAt(i);
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) {
                        escaped.append("\\u00").append(HEX[c >> 4]).append(HEX[c & 0xF]);
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private JsonStrings() {
    }

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private static boolean needsEscape(char c) {
        return c < 0x20 || c == '"' || c == '\\';
    }
}
