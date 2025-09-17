package com.github.kjetilv.uplift.edam.patterns;

import java.time.Duration;
import java.time.Instant;

@SuppressWarnings("unused")
public interface Temporal extends Spanning {

    /**
     * @return Start time of the event
     */
    default Instant startTime() {
        return timespan().start();
    }


    /**
     * @return Duration of the event
     */
    default Duration duration() {
        return timespan().duration();
    }
}
