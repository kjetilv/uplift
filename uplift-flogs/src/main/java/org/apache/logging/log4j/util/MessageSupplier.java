package org.apache.logging.log4j.util;

import org.apache.logging.log4j.message.Message;

/// Gives a message when it is needed. A logger calls it only if the level is enabled.
@FunctionalInterface
public interface MessageSupplier extends java.util.function.Supplier<Message> {

    @Override
    Message get();
}
