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
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("No tokens");
        }
        this.tokens = tokens;
        if (this.tokens.size() < 2) {
            throw new IllegalStateException("Truncated token list");
        }
    }

    Object parse() {
        return tokens.isEmpty() ? null : parseFrom(chomp());
    }

    private Object parseFrom(Token token) {
        Object value = switch (token.type()) {
            case BEGIN_OBJECT -> object();
            case BEGIN_ARRAY -> array();
            case BOOL, STRING, NUMBER, NIL -> token.literal();
            default -> fail(token, BEGIN_OBJECT, BEGIN_ARRAY, BOOL, STRING, NUMBER, NIL);
        };
        return value == NULL_VALUE ? null : value;
    }

    private Map<String, Object> object() {
        Map<String, Object> object = null;
        Token next = chomp();
        while (!next.is(END_OBJECT)) {
            String field = next.is(STRING)
                ? next.literal().toString()
                : fail(next, STRING, END_OBJECT);
            next = chomp(COLON);
            if (object == null) {
                object = new LinkedHashMap<>();
            }
            object.put(field, parseFrom(next));
            next = chomp();
            if (next.is(COMMA)) {
                next = chomp();
            } else if (!next.is(END_OBJECT)) {
                fail(next, END_OBJECT, COMMA);
            }
        }
        return object == null ? Collections.emptyMap() : Collections.unmodifiableMap(object);
    }

    private Collection<Object> array() {
        List<Object> array = null;
        Token next = chomp();
        while (!next.is(END_ARRAY)) {
            if (array == null) {
                array = new ArrayList<>();
            }
            array.add(parseFrom(next));
            next = chomp();
            if (next.is(COMMA)) {
                next = chomp();
            } else if (!next.is(END_ARRAY)) {
                fail(next, END_ARRAY, COMMA);
            }
        }
        return array == null ? Collections.emptyList() : Collections.unmodifiableList(array);
    }

    private Token chomp(TokenType... skippables) {
        for (TokenType skippable: skippables) {
            Token actual = tokens.get(i++);
            if (actual.type() != skippable) {
                fail(actual, skippable);
            }
        }
        return tokens.get(i++);
    }

    private static final Object NULL_VALUE = new Object();

    private static <T> T fail(Token actual, TokenType... expected) {
        throw new ParseException(actual, expected);
    }
}
