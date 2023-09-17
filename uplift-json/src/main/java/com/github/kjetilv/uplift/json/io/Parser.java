package com.github.kjetilv.uplift.json.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.kjetilv.uplift.json.ParseException;
import com.github.kjetilv.uplift.json.tokens.Token;
import com.github.kjetilv.uplift.json.tokens.TokenType;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

public final class Parser {

    private final Token[] tokens;

    private int i;

    public Parser(Token... tokens) {
        if (tokens.length == 0) {
            throw new IllegalArgumentException("Empty token stream");
        }
        this.tokens = tokens;
    }

    public Object parse() {
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
            next = commaOr(END_OBJECT);
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
            next = commaOr(END_ARRAY);
            if (next.is(END_ARRAY)) {
                return Collections.unmodifiableList(array);
            }
        } while (true);
    }

    private Token commaOr(TokenType tokenType) {
        Token next = chomp();
        return next.is(COMMA) ? chomp()
            : next.is(tokenType) ? next
                : fail(next, tokenType, COMMA);
    }

    @SuppressWarnings("SameParameterValue")
    private Token chomp(TokenType skipped) {
        Token actual = tokens[i++];
        if (actual.type() != skipped) {
            fail(actual, skipped);
        }
        return chomp();
    }

    private Token chomp() {
        if (i < tokens.length) {
            return tokens[i++];
        }
        throw new IllegalStateException("End of token stream");
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
