package com.github.kjetilv.uplift.json.tokens;

final class Canonical {

    static char[] chars(int c) {
        return switch (c) {
            case ':' -> COLON;
            case ',' -> COMMA;
            case '{' -> LCURL;
            case '[' -> LBRAC;
            case '}' -> RCURL;
            case ']' -> RBRAC;
            default -> new char[] {(char) c};
        };
    }

    private Canonical() {
    }

    private static final char[] COLON = {':'};

    private static final char[] COMMA = {','};

    private static final char[] LCURL = {'{'};

    private static final char[] LBRAC = {'['};

    private static final char[] RCURL = {'}'};

    private static final char[] RBRAC = {']'};
}
