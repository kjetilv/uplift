package com.github.kjetilv.uplift.json;

class StringSource extends AbstractSource {

    private final String source;

    private int start;

    private int current;

    StringSource(String source) {
        this.source = source;
    }

    @Override
    public String lexeme(boolean quoted) {
        return !quoted ? source.substring(start, current)
            : current == start + 1 ? canonical(source.charAt(current))
                : source.substring(start + 1, current - 1);
    }

    @Override
    public char chomp() {
        return chompAndAdvance();
    }

    @Override
    public char peek() {
        return done() ? '\0' : source.charAt(current);
    }

    @Override
    public char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    @Override
    public void advance() {
        chompAndAdvance();
    }

    @Override
    public void reset() {
        start = current;
    }

    @Override
    public boolean done() {
        return current >= source.length();
    }

    private char chompAndAdvance() {
        try {
            return chomped(source.charAt(current));
        } finally {
            current++;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[start=" + start + ", current=" + current + "]";
    }
}
