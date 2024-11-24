package com.github.kjetilv.uplift.json.tokens;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Source;
import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenType;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.json.Token.*;
import static com.github.kjetilv.uplift.json.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.TokenType.STRING;
import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;

public final class Tokens implements Supplier<Token>, SelfDescribing {

    private final Source source;

    private final TokenTrie tokenTrie;

    public Tokens(Source source) {
        this(source, null);
    }

    private Tokens(Source source, TokenTrie tokenTrie) {
        this.source = source;
        this.tokenTrie = tokenTrie;
    }

    public Stream<Token> stream() {
        return Stream.generate(this).takeWhile(Objects::nonNull);
    }

    public boolean done() {
        spool();
        return source.done();
    }

    @Override
    public Token get() {
        return scan(false);
    }

    public Token getCanonical() {
        return scan(true);
    }

    @Override
    public String bringIt() {
        return MessageFormat.format("`{0}` [{1}:{2}]", source.lexeme(), source.line(), source.column() - 1);
    }

    public Tokens callbacks(Callbacks callbacks) {
//        if (callbacks == null) {
        return this;
//        }
//        Collection<Token> tokens = callbacks.canonicalTokens();
//        if (tokens == null || tokens.isEmpty()) {
//            return this;
//        }
//        return new Tokens(source, new TokenTrie(tokens));
    }

    private Token scan(boolean canonical) {
        while (true) {
            if (source.done()) {
                return null;
            }
            Token token = scanToken(canonical);
            if (token == null) {
                return null;
            }
            if (token == CANONICAL_WHITESPACE) {
                continue;
            }
            return token;
        }
    }

    private Token scanToken(boolean fieldName) {
        source.reset();
        return switch (source.chomp()) {
            case '{' -> BEGIN_OBJECT_TOKEN;
            case ':' -> COLON_TOKEN;
            case ',' -> COMMA_TOKEN;
            case '}' -> END_OBJECT_TOKEN;
            case '[' -> BEGIN_ARRAY_TOKEN;
            case ']' -> END_ARRAY_TOKEN;
            case 't' -> {
                source.skip(4);
                yield TRUE_TOKEN;
            }
            case 'f' -> {
                source.skip(5);
                yield FALSE_TOKEN;
            }
            case 'n' -> {
                source.skip(4);
                yield NULL_TOKEN;
            }
            case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> number();
            case '"' -> fieldName ? fieldName() : stringValue();
            case ' ', '\r', '\t', LN -> spool();
            default -> fail("Unrecognized character");
        };
    }

    private Token stringValue() {
        boolean escaped = false;
        while (source.peek() != '"' && !source.done()) {
            char next = source.peek();
            if (next == LN) {
                fail("Line break in string: " + source.lexeme());
            }
            if (next == ESC) {
                escaped = true;
                source.chomp();
            }
            source.chomp();
        }
        if (source.done()) {
            fail("Unterminated string");
        }
        source.chomp();
        // Trim the surrounding quotes.
        String substring = source.quotedLexeme();
        String literal = escaped
            ? Strings.unquote(substring)
            : substring;
        String lexeme = source.lexeme();
        return token(STRING, literal, lexeme);
    }

    private Token fieldName() {
        while (source.peek() != '"' && !source.done()) {
            char next = source.peek();
            if (next == LN) {
                fail("Line break in string: " + source.lexeme());
            }
            source.chomp();
        }
        if (source.done()) {
            fail("Unterminated string");
        }
        source.chomp();
        return token(STRING, source.quotedLexeme(), source.lexeme());
    }

    private Token number() {
        while (isDigit(source.peek())) {
            source.chomp();
        }
        // Look for a fractional part.
        if (source.peek() == '.' && isDigit(source.peekNext())) {
            // Consume the "."
            do {
                source.chomp();
            } while (isDigit(source.peek()));
        }
        return token(NUMBER, number(source.lexeme()), source.lexeme());
    }

    private Token spool() {
        while (isWhitespace(source.peek())) {
            source.chomp();
        }
        return CANONICAL_WHITESPACE;
    }

    private Token token(TokenType type, Object literal, String lexeme) {
        int line = source.line();
        int col = source.column() - lexeme.length();
        return new Token(type, lexeme, literal, line, col);
    }

    private <T> T fail(String msg) {
        throw new ReadException(msg, bringIt());
    }

    private static final char ESC = '\\';

    private static final char LN = '\n';

    private static Number number(String value) {
        try {
            BigDecimal dec = new BigDecimal(value);
            return dec.scale() == 0 ? dec.longValue() : dec;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse: " + value, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bringIt() + "]";
    }
}
