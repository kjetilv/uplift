package com.github.kjetilv.uplift.json.io;

import module java.base;

record StringSink(StringBuilder sb) implements Sink {

    StringSink {
        Objects.requireNonNull(sb, "sb");
    }

    @Override
    public Mark mark() {
        var length = sb.length();
        return () ->
            length != sb.length();
    }

    @Override
    public void accept(String string) {
        sb.append(string);
    }

    @Override
    public long length() {
        return sb.length();
    }
}
