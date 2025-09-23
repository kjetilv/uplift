package com.github.kjetilv.uplift.json.bytes;

import module java.base;
import module uplift.json;
import module uplift.util;

public final class BytesSourceTokens implements Tokens {

    private final BytesSource bytesSource;

    private final TokenResolver knownTokens;

    private final char[] buffer = new char[1024];

    public BytesSourceTokens(BytesSource bytesSource, TokenResolver knownTokens) {
        this.bytesSource = bytesSource;
        this.knownTokens = knownTokens;
    }

    @Override
    public boolean done() {
        return bytesSource.done();
    }

    @Override
    public Token next(boolean fieldName, boolean canonical) {
        return scanToken(fieldName, canonical);
    }

    private Token scanToken(boolean fieldName, boolean canonical) {
        int c = bytesSource.chomp();
        return switch (c) {
            case ':' -> Token.COLON;
            case ',' -> Token.COMMA;
            case '{' -> Token.BEGIN_OBJECT;
            case '}' -> Token.END_OBJECT;
            case '[' -> Token.BEGIN_ARRAY;
            case ']' -> Token.END_ARRAY;
            case '"' -> fieldName ? fieldToken(canonical) : stringToken();
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-' -> numberToken();
            case 'f' -> _alse();
            case 't' -> _rue();
            case 'n' -> _ull();
            case 0 -> fail("Unexpected end of stream", "`" + bytesSource.lexeme().string() + "`");
            default -> fail("Unrecognized character", "`" + (char) c + "`");
        };
    }

    private Token fieldToken(boolean canonical) {
        Bytes lexeme = bytesSource.spoolField();
        if (canonical) {
            Token.Field resolved = knownTokens.get(lexeme.bytes(), lexeme.offset(), lexeme.length());
            return resolved == null ? Token.SKIP_FIELD : resolved;
        }
        return new Token.Field(lexeme.copyBytes());
    }

    private Token.Str stringToken() {
        return new Token.Str(bytesSource.spoolString().copyBytes());
    }

    private Token numberToken() {
        Bytes lexeme = bytesSource.spoolNumber();
        int len = lexeme.length();
        byte[] bytes = lexeme.bytes();
        for (int i = 0; i < len; i++) {
            buffer[i] = (char) bytes[i];
        }
        try {
            BigDecimal number = new BigDecimal(buffer, 0, len);
            Number numberValue = number.scale() == 0 ? number.longValue() : number;
            return new Token.Number(numberValue);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse: `" + lexeme.string() + "`", e);
        }
    }

    private Token.False _alse() {
        bytesSource.skip('a', 'l', 's', 'e');
        return Token.FALSE;
    }

    private Token.True _rue() {
        bytesSource.skip('r', 'u', 'e');
        return Token.TRUE;
    }

    private Token.Null _ull() {
        bytesSource.skip('u', 'l', 'l');
        return Token.NULL;
    }

    private static Token fail(String msg, String details) {
        throw new ReadException(msg, details);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bytesSource.lexeme().string() + "]";
    }
}
