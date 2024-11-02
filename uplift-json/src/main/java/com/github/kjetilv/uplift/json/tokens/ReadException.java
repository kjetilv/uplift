package com.github.kjetilv.uplift.json.tokens;

public final class ReadException extends RuntimeException {

    ReadException(String msg, String details) {
        super(msg + ": " + details, null);
    }

}
