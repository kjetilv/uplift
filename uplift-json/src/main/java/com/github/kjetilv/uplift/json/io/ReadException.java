package com.github.kjetilv.uplift.json.io;

public final class ReadException extends RuntimeException {

    public ReadException(String msg) {
        super(msg);
    }

    public ReadException(String msg, String details) {
        super(msg + ": " + details);
    }

}
