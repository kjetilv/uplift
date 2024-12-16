package com.github.kjetilv.uplift.json.tokens;

final class Canonical {

    static byte[] bytes(int c) {
        return switch (c) {
            case ':' -> COLON;
            case ',' -> COMMA;
            case '{' -> LCURL;
            case '[' -> LBRAC;
            case '}' -> RCURL;
            case ']' -> RBRAC;
            default -> new byte[] {(byte) c};
        };
    }

    private Canonical() {
    }

    private static final byte[] COLON = {':'};

    private static final byte[] COMMA = {','};

    private static final byte[] LCURL = {'{'};

    private static final byte[] LBRAC = {'['};

    private static final byte[] RCURL = {'}'};

    private static final byte[] RBRAC = {']'};
}
