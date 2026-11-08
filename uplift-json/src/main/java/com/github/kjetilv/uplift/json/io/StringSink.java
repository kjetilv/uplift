package com.github.kjetilv.uplift.json.io;

import module java.base;

public record StringSink(StringBuilder sb) implements Sink {

    public StringSink {
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
    public void accept(char c) {
        sb.append(c);
    }

    @Override
    public void accept(long value) {
        sb.append(value);
    }

    @Override
    public void acceptEscaped(String string) {
        JsonStrings.escape(string, sb);
    }

    @Override
    public void acceptQuoted(String string) {
        JsonStrings.escape(string, sb.append('"')).append('"');
    }

    @Override
    public void acceptField(String field) {
        JsonStrings.escape(field, sb.append('"')).append("\":");
    }

    @Override
    public long length() {
        return sb.length();
    }
}
