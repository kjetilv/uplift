package com.github.kjetilv.uplift.json.tokens;

final class Canonical {

    static String string(char c) {
        return switch (c) {
            case ':' -> ":";
            case ',' -> ",";
            case '{' -> "{";
            case '[' -> "[";
            case '}' -> "}";
            case ']' -> "]";
            default -> String.valueOf(c);
        };
    }

    private Canonical() {
    }
}
