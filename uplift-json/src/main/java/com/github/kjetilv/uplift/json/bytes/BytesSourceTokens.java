package com.github.kjetilv.uplift.json.bytes;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.MemorySegments;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenResolver;
import com.github.kjetilv.uplift.json.Tokens;
import com.github.kjetilv.uplift.json.io.ReadException;

import java.math.BigDecimal;

public final class BytesSourceTokens implements Tokens {

    private final BytesSource bytesSource;

    private final TokenResolver tokenResolver;

    private final char[] buffer = new char[1024];

    public BytesSourceTokens(BytesSource bytesSource, TokenResolver tokenResolver) {
        this.bytesSource = bytesSource;
        this.tokenResolver = tokenResolver;
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
        int b = bytesSource.chomp();
        return switch (b) {
            case ':' -> Token.COLON;
            case ',' -> Token.COMMA;
            case '{' -> Token.BEGIN_OBJECT;
            case '}' -> Token.END_OBJECT;
            case '[' -> Token.BEGIN_ARRAY;
            case ']' -> Token.END_ARRAY;
            case '"' -> fieldName ? fieldToken(canonical) : stringToken();
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-' -> numberToken();
            case 'f' -> falseToken();
            case 't' -> trueToken();
            case 'n' -> nullToken();
            case 0 -> fail("Unexpected end of stream", "`" + bytesSource.lexeme().asString() + "`");
            default -> fail("Unrecognized character", "`" + (char) b + "`");
        };
    }

    private Token fieldToken(boolean canonical) {
        LineSegment lexeme = bytesSource.spoolField();
        if (canonical) {
            Token.Field resolved = tokenResolver.get(lexeme);
            return resolved == null ? Token.SKIP_FIELD : resolved;
        }
        return new Token.Field(bytesSource.lexeme());
    }

    private Token.Str stringToken() {
        return new Token.Str(bytesSource.spoolString());
    }

    private Token numberToken() {
        LineSegment lexeme = bytesSource.spoolNumber();
        MemorySegments.Chars cs = lexeme.asChars(buffer).trim();
        if (cs == MemorySegments.Chars.NULL) {
            throw new IllegalArgumentException("Empty numeric value");
        }
        try {
            BigDecimal number = new BigDecimal(cs.chars(), cs.offset(), cs.length());
            return new Token.Number(number.scale() == 0
                ? number.longValue()
                : number);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse: `" + cs + "`", e);
        }
    }

    private Token falseToken() {
        return skipThen('a', 'l', 's', 'e', Token.FALSE);
    }

    private Token trueToken() {
        return skipThen('r', 'u', 'e', Token.TRUE);
    }

    private Token nullToken() {
        return skipThen('u', 'l', 'l', Token.NULL);
    }

    private Token skipThen(char c0, char c1, char c2, Token token) {
        bytesSource.skip4((byte) c0, (byte) c1, (byte) c2);
        return token;
    }

    @SuppressWarnings("SameParameterValue")
    private Token skipThen(char c0, char c1, char c2, char c3, Token token) {
        bytesSource.skip5((byte) c0, (byte) c1, (byte) c2, (byte) c3);
        return token;
    }

    private static Token fail(String msg, String details) {
        throw new ReadException(msg, details);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bytesSource.lexeme().asString() + "]";
    }
}
