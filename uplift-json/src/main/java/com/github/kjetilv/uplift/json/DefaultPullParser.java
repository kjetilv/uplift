package com.github.kjetilv.uplift.json;

import static com.github.kjetilv.uplift.json.TokenType.*;

final class DefaultPullParser implements PullParser {

    @Override
    public Callbacks pull(Tokens tokens, Callbacks callbacks) {
        return processValue(tokens, tokens.next(), callbacks);
    }

    private Callbacks processValue(Tokens tokens, Token token, Callbacks callbacks) {
        return switch (token) {
            case Token.Str str -> callbacks.string(str);
            case Token.BeginObject _ -> processObject(tokens, callbacks);
            case Token.BeginArray _ -> processArray(tokens, callbacks);
            case Token.Null _ -> callbacks.nuul();
            case Token.True _ -> callbacks.bool(true);
            case Token.False _ -> callbacks.bool(false);
            case Token.Number number -> callbacks.number(number);
            default -> failParse(
                token,
                BEGIN_OBJECT,
                BEGIN_ARRAY,
                NULL,
                STRING,
                NUMBER,
                BOOL
            );
        };
    }

    private Callbacks processObject(Tokens tokens, Callbacks initial) {
        Callbacks callbacks = initial.objectStarted();
        boolean canonical = callbacks.tokenResolver().isPresent();
        Token next = tokens.nextField(canonical);
        while (next != Token.END_OBJECT) {
            switch (next) {
                case Token.Field fieldToken -> callbacks = processField(
                    tokens,
                    fieldToken,
                    callbacks
                );
                case Token.SkipField _ -> skip(tokens);
                default -> failParse(
                    next,
                    STRING,
                    TokenType.END_OBJECT
                );
            }
            next = commaOr(tokens, Token.END_OBJECT, canonical);
        }
        return callbacks.objectEnded();
    }

    private Callbacks processArray(Tokens tokens, Callbacks initial) {
        Callbacks callbacks = initial.arrayStarted();
        Token next = tokens.next();
        while (next != Token.END_ARRAY) {
            callbacks = processValue(tokens, next, callbacks);
            next = commaOr(tokens, Token.END_ARRAY, false);
        }
        return callbacks.arrayEnded();
    }

    private Callbacks processField(Tokens tokens, Token.Field field, Callbacks initial) {
        try {
            Callbacks callbacks = initial.field(field);
            tokens.skipNext(Token.COLON);
            Token valueToken = tokens.next();
            return processValue(tokens, valueToken, callbacks);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set `" + field.value() + "`", e);
        }
    }

    private void skip(Tokens tokens) {
        Token token = tokens.skipNext(Token.COLON).next();
        if (token == Token.BEGIN_OBJECT) {
            skipStructure(tokens, Token.BEGIN_OBJECT, Token.END_OBJECT);
        } else if (token == Token.BEGIN_ARRAY) {
            skipStructure(tokens, Token.BEGIN_ARRAY, Token.END_ARRAY);
        } else if (!token.isValue()) {
            failParse(token, BEGIN_OBJECT, BEGIN_ARRAY, BOOL, NULL, NUMBER, STRING);
        }
    }

    private void skipStructure(Tokens tokens, Token opening, Token closing) {
        int levels = 0;
        do {
            Token next = tokens.next();
            if (next == closing) {
                levels--;
            } else if (next == opening) {
                levels++;
            }
        } while (levels >= 0);
    }

    private Token commaOr(Tokens tokens, Token closing, boolean canonical) {
        Token token = tokens.next();
        if (token == Token.COMMA) {
            Token nextToken = tokens.next(closing == Token.END_OBJECT, canonical);
            return nextToken == closing
                ? failParse(nextToken, TokenType.valueTokens())
                : nextToken;
        }
        return token == closing
            ? token
            : failParse(token, COMMA);
    }

    private <T> T failParse(Token actual, TokenType... expected) {
        throw new ParseException(this, actual, expected);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}