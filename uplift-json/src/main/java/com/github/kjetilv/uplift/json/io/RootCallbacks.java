package com.github.kjetilv.uplift.json.io;

import com.github.kjetilv.uplift.json.Callbacks;

import java.util.regex.Pattern;

class RootCallbacks extends SinkCallbacks {

    RootCallbacks(Sink sink) {
        this(null, sink);
    }

    RootCallbacks(Callbacks parent, Sink sink) {
        super(parent, sink);
    }

    @Override
    public Callbacks objectStarted() {
        return new ObjectCallbacks(this, sink());
    }

    @Override
    public Callbacks arrayStarted() {
        return new ArrayCallbacks(this, sink());
    }

    @Override
    public Callbacks string(String value) {
        sink().accept("\"")
            .accept(value.indexOf('"') >= 0
                ? QUOTE.matcher(value).replaceAll("\\\\\"")
                : value)
            .accept("\"");
        return parent();
    }

    @Override
    public <N extends Number> Callbacks number(N number) {
        sink().accept(number);
        return parent();
    }

    @Override
    public Callbacks nil() {
        sink().accept(Canonical.NULL);
        return parent();
    }

    @Override
    public Callbacks arrayEnded() {
        sink().accept("]");
        return this;
    }

    private static final Pattern QUOTE = Pattern.compile("\"");

    private static final class ArrayCallbacks extends SinkCallbacks {

        private ArrayCallbacks(SinkCallbacks parent, Sink sink) {
            super(parent, sink);
            sink.accept("[");
        }

        @Override
        public Callbacks objectStarted() {
            return new RootCallbacks(this, sink()).objectStarted();
        }

        @Override
        public Callbacks arrayStarted() {
            return new RootCallbacks(this, sink()).arrayStarted();
        }

        @Override
        public Callbacks string(String string) {
            return new RootCallbacks(this, sink()).string(string);
        }

        @Override
        public <N extends Number> Callbacks number(N number) {
            return new RootCallbacks(this, sink()).number(number);
        }

        @Override
        public Callbacks bool(boolean bool) {
            return new RootCallbacks(this, sink()).bool(bool);
        }

        @Override
        public Callbacks nil() {
            return new RootCallbacks(this, sink()).nil();
        }

        @Override
        public Callbacks arrayEnded() {
            sink().accept("]");
            return parent();
        }
    }

    private static final class ObjectCallbacks extends SinkCallbacks {

        private ObjectCallbacks(SinkCallbacks parent, Sink sink) {
            super(parent, sink);
            sink.accept("{");
        }

        @Override
        public Callbacks field(String name) {
            sink().accept("\"" + name + "\":");
            return new RootCallbacks(this, sink());
        }

        @Override
        public Callbacks objectEnded() {
            sink().accept("}");
            return parent();
        }
    }
}
