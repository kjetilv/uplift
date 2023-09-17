package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Events;

public class JsonCallback implements Events.Callbacks<JsonCallback> {

    public interface JsonEvents {

        JsonEvents arrayStarted();

        JsonEvents arrayValue();

        JsonEvents arrayEnded();
    }
}
