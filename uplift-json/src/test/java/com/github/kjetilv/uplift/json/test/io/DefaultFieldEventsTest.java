package com.github.kjetilv.uplift.json.test.io;

import com.github.kjetilv.uplift.json.io.DefaultFieldEvents;
import com.github.kjetilv.uplift.json.io.StringSink;
import org.junit.jupiter.api.Test;

class DefaultFieldEventsTest {

    @Test
    void writeString() {
        var sb = new StringBuilder();
        var events = new DefaultFieldEvents(new StringSink(sb));
        events.string("bar", "zot");
        events.string("foo", "belle de nature - The New Rijksmuseum - Filmworks\\21 -");
        events.done();
        System.out.println(sb);
    }

}
