package org.slf4j.helpers;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

@SuppressWarnings("unused")
public record NOPLoggerFactory() implements ILoggerFactory {

    public Logger getLogger(String name) {
        return NOP_LOGGER;
    }

    private static final NOPLogger NOP_LOGGER = new NOPLogger();
}
