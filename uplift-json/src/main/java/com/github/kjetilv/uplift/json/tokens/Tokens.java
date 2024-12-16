package com.github.kjetilv.uplift.json.tokens;

import com.github.kjetilv.uplift.json.Source;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenResolver;

import java.lang.Number;
import java.lang.String;
import java.math.BigDecimal;

import static com.github.kjetilv.uplift.json.Token.*;

public final class Tokens {

    private final Source source;

    private final TokenResolver tokenResolver;

    public Tokens(Source source, TokenResolver tokenResolver) {
        this.source = source;
        this.tokenResolver = tokenResolver;
    }

    public boolean done() {
        return source.done();
    }

    public Token next() {
        return scanToken(false, false);
    }

    public Token next(boolean name) {
        return scanToken(name, false);
    }

    public Token next(boolean name, boolean canonical) {
        return scanToken(name, canonical);
    }

    public Token nextField(boolean canonical) {
        return scanToken(true, canonical);
    }

    public void skipNext(Token expected) {
        while (true) {
            Token token = scanToken(false, false);
            if (token == expected) {
                return;
            }
            fail("Unexpected token " + token + ", expected " + expected);
        }
    }

    private Token scanToken(boolean fieldName, boolean canonical) {
        if (source.done()) {
            return fail("Unexpected end of stream");
        }
        source.reset();
        byte c = source.chomp();
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
            case '0',
                 '1',
                 '2',
                 '3',
                 '4',
                 '5',
                 '6',
                 '7',
                 '8',
                 '9',
                 '.',
                 '-' -> numberToken();
            default -> fail(c);
        };
    }

    private Token.String stringToken() {
        source.spoolString();
        return new Token.String(source.lexemeCopy());
    }

    private Field fieldToken(boolean canonical) {
        source.spoolField();
        if (canonical) {
            Source.Loan loan = source.lexemeLoan();
            Field found = tokenResolver.get(loan.loaned(), loan.offset(), loan.length());
            if (found != null) {
                return found;
            }
        }
        return new Field(source.lexemeCopy());
    }

    private Token skipThen(byte r, byte u, byte e, Token token) {
        source.skip4(r, u, e);
        return token;
    }

    @SuppressWarnings("SameParameterValue")
    private Token skipThen(byte a, byte l, byte s, byte e, Token token) {
        source.skip5(a, l, s, e);
        return token;
    }

    private Token numberToken() {
        source.spoolNumber();
        Source.Loan loan = source.lexemeLoan();
        try {
            //            int length = loan.length();
//            char[] chars = new char[length];
//            for (int i = 0; i < length; i++) {
//                chars[i] = (char) bytes[i];
//            }
            BigDecimal dec = new BigDecimal(new String(
                loan.loaned(),
                loan.offset(),
                loan.length()
            ));
            Number number = dec.scale() == 0 ? dec.longValue() : dec;
            return new Token.Number(number);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse: `" + loan.string() + "`", e);
        }
    }

    private <T> T fail(String msg) {
        throw new ReadException(msg, "`" + new String(source.lexemeCopy()) + "`");
    }

    private <T> T fail(byte c) {
        throw new ReadException("Unrecognized character", "`" + (char)c + "`");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + new String(source.lexemeCopy()) + "]";
    }
}
