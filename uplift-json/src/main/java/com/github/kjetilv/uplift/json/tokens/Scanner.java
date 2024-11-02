package com.github.kjetilv.uplift.json.tokens;

import java.io.InputStream;
import java.io.Reader;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.kjetilv.uplift.json.tokens.TokenType.WHITESPACE;

public final class Scanner extends Spliterators.AbstractSpliterator<Token> {

    public static Stream<Token> tokens(String source) {
        return tokenStream(new CharSequenceSource(source));
    }

    public static Stream<Token> tokens(InputStream source) {
        return tokenStream(new InputStreamSource(source));
    }

    public static Stream<Token> tokens(Reader source) {
        return tokenStream(new CharsSource(source));
    }

    public static Stream<Token> tokenStream(Source source) {
        return StreamSupport.stream(
            new Scanner(new Tokens(source)),
            false
        );
    }

    private final Supplier<Token> source;

    private final AtomicBoolean done = new AtomicBoolean();

    public Scanner(Supplier<Token> source) {
        super(Long.MAX_VALUE, IMMUTABLE | ORDERED);
        this.source = source;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Token> action) {
        if (done.get()) {
            return false;
        }
        Token token = source.get();
        if (token == null) {
            done.set(true);
            return false;
        }
        if (!token.is(WHITESPACE)) {
            action.accept(token);
        }
        return true;
    }

    static final String CANONICAL_TRUE = "true";

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + source + "]";
    }
}
