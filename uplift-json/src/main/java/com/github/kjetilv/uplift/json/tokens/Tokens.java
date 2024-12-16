package com.github.kjetilv.uplift.json.tokens;

import com.github.kjetilv.uplift.json.Source;
import com.github.kjetilv.uplift.json.Token;

import java.lang.Number;
import java.lang.String;
import java.math.BigDecimal;
import java.util.function.Function;

import static com.github.kjetilv.uplift.json.Token.*;

public final class Tokens {

    private final Source source;

    private final Function<char[], Field> tokenResolver;

    public Tokens(Source source, Function<char[], Token.Field> tokenResolver) {
        this.source = source;
        this.tokenResolver = tokenResolver;
    }

    public boolean done() {
        return source.done();
    }

    public Token next() {
        return scanToken(false);
    }

    public Token next(boolean name) {
        return scanToken(name);
    }

    public Token nextName() {
        return scanToken(true);
    }

    public void skipNext(Token expected) {
        while (true) {
            Token token = scanToken(false);
            if (token == expected) {
                return;
            }
            fail("Unexpected token " + token + ", expected " + expected);
        }
    }

    private Token scanToken(boolean fieldName) {
        if (source.done()) {
            return fail("Unexpected end of stream");
        }
        source.reset();
        char c = source.chomp();
        return switch (c) {
            case ':' -> COLON;
            case ',' -> COMMA;
            case '{' -> BEGIN_OBJECT;
            case '}' -> END_OBJECT;
            case '[' -> BEGIN_ARRAY;
            case ']' -> END_ARRAY;
            case '"' -> fieldName
                ? fieldToken()
                : stringToken();
            case 'f' -> skipThen(
                'a',
                'l',
                's',
                'e',
                FALSE
            );
            case 't' -> skipThen(
                'r',
                'u',
                'e',
                TRUE
            );
            case 'n' -> skipThen(
                'u',
                'l',
                'l',
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
        return new Token.String(source.lexeme());
    }

    private Field fieldToken() {
        source.spoolField();
        char[] chars = source.lexeme();
        return tokenResolver.apply(chars);
//        Source.Loan chars = source.loanLexeme();
//        return tokenResolver.apply(chars.loaned());
    }

    private Token skipThen(char r, char u, char e, Token token) {
        source.skip4(r, u, e);
        return token;
    }

    @SuppressWarnings("SameParameterValue")
    private Token skipThen(char a, char l, char s, char e, Token token) {
        source.skip5(a, l, s, e);
        return token;
    }

    private Token numberToken() {
        source.spoolNumber();
        Source.Loan loan = source.loanLexeme();
        try {
            BigDecimal dec = new BigDecimal(loan.loaned(), 0, loan.length());
            Number number = dec.scale() == 0 ? dec.longValue() : dec;
            return new Token.Number(number);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse: `" + loan.string() + "`", e);
        }
    }

    private <T> T fail(String msg) {
        throw new ReadException(msg, "`" + new String(source.lexeme()) + "`");
    }

    private <T> T fail(char c) {
        throw new ReadException("Unrecognized character", "`" + c + "`");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + new String(source.lexeme()) + "]";
    }
}
