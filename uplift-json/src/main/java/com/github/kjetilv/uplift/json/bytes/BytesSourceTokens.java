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
        return switch (bytesSource.chomp()) {
            case ':' -> Token.COLON;
            case ',' -> Token.COMMA;
            case '{' -> Token.BEGIN_OBJECT;
            case '}' -> Token.END_OBJECT;
            case '[' -> Token.BEGIN_ARRAY;
            case ']' -> Token.END_ARRAY;
            case '"' -> fieldName ? fieldToken(canonical) : stringToken();
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-' -> numberToken();
            case 'f' -> alse();
            case 't' -> rue();
            case 'n' -> ull();
            case 0 -> fail("Unexpected end of stream", "`" + bytesSource.lexeme().asString() + "`");
            case int x -> fail("Unrecognized character", "`" + (char) x + "`");
        };
    }

    private Token fieldToken(boolean canonical) {
        LineSegment lexeme = bytesSource.spoolField();
        if (canonical) {
            Token.Field resolved = knownTokens.get(lexeme);
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

    private Token.False alse() {
        bytesSource.skip5(A, L, S, E);
        return Token.FALSE;
    }

    private Token.True rue() {
        bytesSource.skip4(R, U, E);
        return Token.TRUE;
    }

    private Token.Null ull() {
        bytesSource.skip4(U, L, L);
        return Token.NULL;
    }

    private static final byte A = (byte) 'a';

    private static final byte E = (byte) 'e';

    private static final byte L = (byte) 'l';

    private static final byte R = (byte) 'r';

    private static final byte S = (byte) 's';

    private static final byte U = (byte) 'u';

    private static Token fail(String msg, String details) {
        throw new ReadException(msg, details);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bytesSource.lexeme().asString() + "]";
    }
}
