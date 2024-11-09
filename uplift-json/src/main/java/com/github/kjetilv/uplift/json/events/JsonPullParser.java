package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.ParseException;
import com.github.kjetilv.uplift.json.tokens.Token;
import com.github.kjetilv.uplift.json.tokens.TokenType;
import com.github.kjetilv.uplift.json.tokens.Tokens;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

final class JsonPullParser {

    private final Tokens tokens;

    JsonPullParser(Tokens tokens) {
        this.tokens = tokens;
    }

    Callbacks pull(Callbacks callbacks) {
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
            c = processValue(skipColon(), c.field(next.literalString()));
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

    private Token skipColon() {
        Token token = tokens.get();
        return switch (token.type()) {
            case COLON -> tokens.get();
            case null, default -> fail(token, COLON);
        };
    }

    private <T> T fail(Token actual, TokenType... expected) {
        throw new ParseException(this, actual, expected);
    }
}