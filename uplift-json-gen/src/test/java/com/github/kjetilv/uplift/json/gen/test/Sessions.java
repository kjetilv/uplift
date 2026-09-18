package com.github.kjetilv.uplift.json.gen.test;

import module java.base;

public final class Sessions {

    public static Session session(String source, Path tempDirectory) {
        return SessionsImpl.session(source, tempDirectory);
    }
    private Sessions() {
    }
}
