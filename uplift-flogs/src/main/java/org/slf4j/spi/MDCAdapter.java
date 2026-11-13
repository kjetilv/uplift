package org.slf4j.spi;

import module java.base;

/// The MDC operations of SLF4J. The deque operations of SLF4J 2 (`pushByKey` and others) are not supported.
@SuppressWarnings("unused")
public interface MDCAdapter {

    void put(String key, String val);

    String get(String key);

    void remove(String key);

    void clear();

    Map<String, String> getCopyOfContextMap();

    void setContextMap(Map<String, String> contextMap);
}
