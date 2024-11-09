package com.github.kjetilv.uplift.json.io;

public sealed interface Sink permits StreamSink, StringSink {

    default Sink accept(Object obj) {
        return accept(obj.toString());
    }

    default Sink accept(boolean bool) {
        return accept(bool ? Canonical.TRUE : Canonical.FALSE);
    }

    default Sink accept(Number number) {
        return accept(number.toString());
    }

    Sink accept(String str);

    Mark mark();

    int length();

    @FunctionalInterface
    interface Mark {

        boolean moved();
    }
}
