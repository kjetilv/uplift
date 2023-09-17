package com.github.kjetilv.uplift.json.io;

public interface Sink {

    @FunctionalInterface
    interface Mark {

        boolean moved();
    }

    default Sink accept(Object obj) {
        return accept(obj.toString());
    }

    default Sink accept(boolean bool) {
        return accept(bool ? Canonical.TRUE : Canonical.FALSE);
    }

    Sink accept(String str);

    Mark mark();

    int length();
}