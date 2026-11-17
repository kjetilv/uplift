package org.apache.logging.log4j.message;

/// A log message. Flogs logs the [#getFormattedMessage()], with the [#getThrowable()] if no other throwable
/// is given.
@SuppressWarnings("unused")
public interface Message {

    String getFormattedMessage();

    default String getFormat() {
        return getFormattedMessage();
    }

    default Object[] getParameters() {
        return null;
    }

    default Throwable getThrowable() {
        return null;
    }
}
