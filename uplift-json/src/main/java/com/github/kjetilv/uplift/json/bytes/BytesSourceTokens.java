package com.github.kjetilv.uplift.json.bytes;

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
        if (bytesSource.done()) {
            throw new ReadException("Unexpected end of stream", "`" + bytesSource.lexeme().asString() + "`");
        }
        bytesSource.reset();
        byte c = bytesSource.chomp();
        return switch (c) {
            case ':' -> COLON;
            case ',' -> COMMA;
            case '{' -> BEGIN_OBJECT;
            case '}' -> END_OBJECT;
            case '[' -> BEGIN_ARRAY;
            case ']' -> END_ARRAY;
            case '"' -> fieldName
                ? fieldToken(canonical)
                : stringToken();
            case 'f' -> skipThen(
                (byte) 'a',
                (byte) 'l',
                (byte) 's',
                (byte) 'e',
                FALSE
            );
            case 't' -> skipThen(
                (byte) 'r',
                (byte) 'u',
                (byte) 'e',
                TRUE
            );
            case 'n' -> skipThen(
                (byte) 'u',
                (byte) 'l',
                (byte) 'l',
                NULL
            );
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-' -> numberToken();
            default -> throw new ReadException("Unrecognized character", "`" + (char) c + "`");
        };
    }

    private Token.String stringToken() {
        bytesSource.spoolString();
        return new Token.String(bytesSource.lexeme());
    }

    private Field fieldToken(boolean canonical) {
        bytesSource.spoolField();
        if (canonical) {
            Field found = tokenResolver.get(bytesSource.lexeme());
            if (found != null) {
                return found;
            }
        }
        return new Field(bytesSource.lexeme());
    }

    private Token skipThen(byte r, byte u, byte e, Token token) {
        bytesSource.skip4(r, u, e);
        return token;
    }

    @SuppressWarnings("SameParameterValue")
    private Token skipThen(byte a, byte l, byte s, byte e, Token token) {
        bytesSource.skip5(a, l, s, e);
        return token;
    }

    private Token numberToken() {
        bytesSource.spoolNumber();
        String string = bytesSource.lexeme().asString();
        try {
            BigDecimal number = new BigDecimal(string.trim());
            return new Token.Number(number.scale() == 0
                ? number.longValue()
                : number);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse: `" + string + "`", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bytesSource.lexeme().asString() + "]";
    }
}
