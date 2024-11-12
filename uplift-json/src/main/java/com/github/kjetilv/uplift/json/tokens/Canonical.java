package com.github.kjetilv.uplift.json.tokens;

final class Canonical {

    static String string(int c) {
        return switch (c) {
            case ':' -> ":";
            case ',' -> ",";
            case '{' -> "{";
            case '[' -> "[";
            case '}' -> "}";
            case ']' -> "]";
            default -> Character.toString(c);
        };
    }

    private Canonical() {
    }
}
