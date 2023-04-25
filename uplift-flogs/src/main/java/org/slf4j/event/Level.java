package org.slf4j.event;

@SuppressWarnings("unused")
public enum Level {

    ERROR, WARN, INFO, DEBUG, TRACE;

    public int toInt() {
        return ordinal();
    }
}
