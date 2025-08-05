package com.github.kjetilv.uplift.json;

import java.util.Objects;
import java.util.Optional;

public record TeeingCallbacks(Callbacks tee, Callbacks delegate) implements Callbacks {

    public TeeingCallbacks {
        Objects.requireNonNull(tee, "tee");
        Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public Callbacks objectStarted() {
        tee.objectStarted();
        return delegate.objectStarted();
    }

    @Override
    public Callbacks field(Token.Field token) {
        tee.field(token);
        return delegate.field(token);
    }

    @Override
    public Callbacks objectEnded() {
        tee.objectEnded();
        return delegate.objectEnded();
    }

    @Override
    public Callbacks arrayStarted() {
        tee.arrayStarted();
        return delegate.arrayStarted();
    }

    @Override
    public Callbacks string(Token.Str str) {
        tee.string(str);
        return delegate.string(str);
    }

    @Override
    public Callbacks number(Token.Number number) {
        tee.number(number);
        return delegate.number(number);
    }

    @Override
    public Callbacks bool(boolean bool) {
        tee.bool(bool);
        return delegate.bool(bool);
    }

    @Override
    public Callbacks nuul() {
        tee.nuul();
        return delegate.nuul();
    }

    @Override
    public Callbacks arrayEnded() {
        tee.arrayEnded();
        return delegate.arrayEnded();
    }

    @Override
    public boolean multi() {
        return delegate.multi();
    }

    @Override
    public Callbacks line() {
        tee.line();
        return delegate.line();
    }

    @Override
    public Optional<TokenResolver> tokenResolver() {
        return delegate.tokenResolver();
    }
}
