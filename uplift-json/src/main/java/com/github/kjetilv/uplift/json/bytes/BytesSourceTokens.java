package com.github.kjetilv.uplift.json.bytes;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenResolver;
import com.github.kjetilv.uplift.json.Tokens;
import com.github.kjetilv.uplift.json.io.ReadException;

import java.lang.String;
import java.math.BigDecimal;

import static com.github.kjetilv.uplift.json.Token.*;

public final class BytesSourceTokens implements Tokens {

    private final BytesSource bytesSource;

    private final TokenResolver tokenResolver;

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
        byte c = bytesSource.chomp();
        return switch (c) {
            case ':' -> COLON;
            case ',' -> COMMA;
            case '{' -> BEGIN_OBJECT;
            case '}' -> END_OBJECT;
            case '[' -> BEGIN_ARRAY;
            case ']' -> END_ARRAY;
            case '"' -> fieldName ? fieldToken(canonical) : stringToken();
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-' -> numberToken();
            case 'f' -> falseToken();
            case 't' -> trueToken();
            case 'n' -> nullToken();
            case 0 -> fail("Unexpected end of stream", "`" + bytesSource.lexeme().asString() + "`");
            default -> fail("Unrecognized character", "`" + (char) c + "`");
        };
    }

    private Field fieldToken(boolean canonical) {
        LineSegment lexeme = bytesSource.spoolField();
        if (canonical) {
            Field found = tokenResolver.get(lexeme);
            if (found != null) {
                return found;
            }
        }
        return new Field(bytesSource.lexeme());
    }

    private Token.String stringToken() {
        return new Token.String(bytesSource.spoolString());
    }

    private Token numberToken() {
        LineSegment lexeme = bytesSource.spoolNumber();
        String numberString = lexeme.asString();
        try {
            BigDecimal number = new BigDecimal(numberString.trim());
            return new Token.Number(number.scale() == 0
                ? number.longValue()
                : number);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse: `" + numberString + "`", e);
        }
    }

    private Token falseToken() {
        return skipThen('a', 'l', 's', 'e', FALSE);
    }

    private Token trueToken() {
        return skipThen('r', 'u', 'e', TRUE);
    }

    private Token nullToken() {
        return skipThen('u', 'l', 'l', NULL);
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
