package com.github.kjetilv.uplift.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.kjetilv.uplift.json.TokenType.*;

final class Parser {

    private final List<Token> tokens;

    private int i;

    Parser(List<Token> tokens) {
        if (tokens == null ||  tokens.isEmpty()) {
            throw new IllegalArgumentException("No tokens");
        }
        this.tokens = tokens;
        if (this.tokens.size() < 2) {
            throw new IllegalStateException("Truncated token list");
        }
    }

    Object parse() {
        if (tokens.isEmpty()) {
            return null;
        }
        Token token = chomp();
        Object value = switch (token.type()) {
            case BEGIN_OBJECT -> object();
            case BEGIN_ARRAY -> array();
            case BOOL, STRING, NUMBER, NIL -> token.literal();
            default -> fail(token, BEGIN_OBJECT, BEGIN_ARRAY, BOOL, STRING, NUMBER, NIL);
        };
        return value == NULL_VALUE ? null : value;
    }

    private Map<String, Object> object() {
        if (peek().isNot(END_OBJECT)) {
            Map<String, Object> object = new LinkedHashMap<>();
            while (peek().isNot(END_OBJECT)) {
                String field = readField();
                object.put(field, parse());
                Token next = peek();
                if (next.comma()) {
                    chomp();
                } else if (next.isNot(END_OBJECT)) {
                    fail(peek(), END_OBJECT, COMMA);
                }
            }
            chomp();
            return object;
        }
        chomp();
        return Collections.emptyMap();
    }

    private String readField() {
        String field = chomp(STRING).literal().toString();
        chomp(COLON);
        return field;
    }

    private Collection<Object> array() {
        Collection<Object> array = new ArrayList<>();
        while (peek().isNot(END_ARRAY)) {
            array.add(parse());
            Token next = peek();
            if (next.comma()) {
                chomp();
            } else if (next.isNot(END_ARRAY)) {
                fail(peek(), END_ARRAY, COMMA);
            }
        }
        chomp();
        return array;
    }

    private Token chomp() {
        return tokens.get(i++);
    }

    private Token chomp(TokenType expected) {
        try {
            Token token = tokens.get(i);
            return token.type() == expected
                ? token
                : fail(token, expected);
        } finally {
            i++;
        }
    }

    private Token peek() {
        return tokens.get(i);
    }

    private static final Object NULL_VALUE = new Object();

    private static <T> T fail(Token actual, TokenType... expected) {
        throw new ParseException(actual, expected);
    }
}
