package org.slf4j;

@FunctionalInterface
@SuppressWarnings("unused")
public interface ILoggerFactory {

    Logger getLogger(String name);
}
