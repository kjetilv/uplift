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
    }

    Object parse() {
        if (tokens.isEmpty()) {
            throw new IllegalStateException("End of token stream");
        }
        return parseFrom(chomp());
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
        Token next = chomp();
        if (next.is(END_OBJECT)) {
            return Collections.emptyMap();
        }
        Map<String, Object> object = new LinkedHashMap<>();
        do {
            object.put(
                string(next),
                parseFrom(chomp(COLON))
            );
            next = expectCommaOr(END_OBJECT);
            if (next.is(END_OBJECT)) {
                return Collections.unmodifiableMap(object);
            }
        } while (true);
    }

    private Collection<Object> array() {
        Token next = chomp();
        if (next.is(END_ARRAY)) {
            return Collections.emptyList();
        }
        List<Object> array = new ArrayList<>();
        do {
            array.add(parseFrom(next));
            next = expectCommaOr(END_ARRAY);
            if (next.is(END_ARRAY)) {
                return Collections.unmodifiableList(array);
            }
        } while (true);
    }

    private Token expectCommaOr(TokenType tokenType) {
        Token next = chomp();
        return next.is(COMMA) ? chomp()
            : next.is(tokenType) ? next
                : fail(next, tokenType, COMMA);
    }

    private Token chomp(TokenType skipped) {
        Token actual = tokens.get(i++);
        if (actual.type() != skipped) {
            fail(actual, skipped);
        }
        return chomp();
    }

    private Token chomp() {
        return tokens.get(i++);
    }

    private static final Object NULL_VALUE = new Object();

    private static String string(Token next) {
        return next.is(STRING)
            ? next.literalString()
            : fail(next, STRING, END_OBJECT);
    }

    private static <T> T fail(Token actual, TokenType... expected) {
        throw new ParseException(actual, expected);
    }
}
