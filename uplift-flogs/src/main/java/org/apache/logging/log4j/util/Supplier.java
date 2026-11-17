package org.apache.logging.log4j.util;

/// Gives a value when it is needed. A logger calls it only if the level is enabled.
@FunctionalInterface
public interface Supplier<T> extends java.util.function.Supplier<T> {

    @Override
    T get();
}
