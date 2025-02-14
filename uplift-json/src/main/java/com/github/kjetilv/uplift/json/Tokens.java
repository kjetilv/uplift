package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.io.ReadException;

public interface Tokens {

    default Token next() {
        return next(false, false);
    }

    default Token next(boolean fieldName) {
        return next(fieldName, false);
    }

    default Token nextField(boolean canonical) {
        return next(true, canonical);
    }

    default Tokens skipNext(Token expected) {
        Token token = next();
        if (token == expected) {
            return this;
        }
        throw new ReadException("Unexpected token " + token + ", expected " + expected);
    }

    Token next(boolean fieldName, boolean canonical);

    boolean done();
}
