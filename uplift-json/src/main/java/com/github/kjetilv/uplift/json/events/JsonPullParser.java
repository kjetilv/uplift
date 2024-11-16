package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.ParseException;
import com.github.kjetilv.uplift.json.tokens.Token;
import com.github.kjetilv.uplift.json.tokens.TokenType;
import com.github.kjetilv.uplift.json.tokens.Tokens;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

public final class JsonPullParser {

    private final Tokens tokens;

    public JsonPullParser(Tokens tokens) {
        this.tokens = tokens;
    }

    public Callbacks pull(Callbacks callbacks) {
        return processValue(tokens.get(), callbacks);
    }

    private Callbacks processValue(Token token, Callbacks callbacks) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> processObject(callbacks);
            case BEGIN_ARRAY -> processArray(callbacks);
            case NULL -> callbacks.null_();
            case STRING -> callbacks.string(token.literalString());
            case NUMBER -> callbacks.number(token.literalNumber());
            case BOOL -> callbacks.bool(token.literalTruth());
            case WHITESPACE -> callbacks;
            default -> fail(token, BEGIN_OBJECT, BEGIN_ARRAY, NULL, STRING, NUMBER, BOOL);
        };
    }

    private Callbacks processObject(Callbacks callbacks) {
        Callbacks c = callbacks.objectStarted();
        Token next = tokens.get();
        while (next.not(END_OBJECT)) {
            if (next.not(STRING)) {
                return fail(next, STRING, END_OBJECT);
            }
            Callbacks field = c.field(next.literalString());
            requireColon();
            c = processValue(tokens.get(), field);
            next = separatorOr(tokens.get(), END_OBJECT);
        }
        return c.objectEnded();
    }

    private Callbacks processArray(Callbacks callbacks) {
        Callbacks c = callbacks.arrayStarted();
        Token next = tokens.get();
        while (next.not(END_ARRAY)) {
            c = processValue(next, c);
            next = separatorOr(tokens.get(), END_ARRAY);
        }
        return c.arrayEnded();
    }

    private Token separatorOr(Token token, TokenType close) {
        if (token.is(COMMA)) {
            Token nextToken = tokens.get();
            return nextToken.not(close)
                ? nextToken
                : fail(nextToken, close);
        }
        return token.is(close)
            ? token
            : fail(token, COMMA, close);
    }

    private void requireColon() {
        Token token = tokens.get();
        if (token.not(COLON)) {
            fail(token, COLON);
        }
    }

    private <T> T fail(Token actual, TokenType... expected) {
        throw new ParseException(this, actual, expected);
    }
}