package com.github.kjetilv.uplift.edam.patterns;

import module java.base;

@SuppressWarnings("unused")
public interface Timelined extends Spanning {

    /// @return Start time of the event
    default Instant startTime() {
        return timespan().start();
    }


    /// @return Duration of the event
    default Duration duration() {
        return timespan().duration();
    }
}
