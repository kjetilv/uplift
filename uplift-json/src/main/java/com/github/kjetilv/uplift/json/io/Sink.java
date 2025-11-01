package com.github.kjetilv.uplift.json.io;

import java.io.ByteArrayOutputStream;

public sealed interface Sink permits StreamSink, StringSink {

    static Sink stream(ByteArrayOutputStream baos) {
        return new StreamSink(baos);
    }

    static Sink stream(StringBuilder stringBuilder) {
        return new StringSink(stringBuilder);
    }

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
