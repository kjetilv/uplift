package com.github.kjetilv.uplift.json.io;

import com.github.kjetilv.uplift.json.Callbacks;

class SinkCallbacks extends UnsupportedCallbacks {

    private final Sink sink;

    private final Callbacks parent;

    SinkCallbacks(Callbacks parent, Sink sink) {
        this.parent = parent;
        this.sink = sink;
    }

    protected Callbacks parent() {
        return parent;
    }

    protected Sink sink() {
        return sink;
    }
}
