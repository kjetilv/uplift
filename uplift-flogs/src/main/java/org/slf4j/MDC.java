package org.slf4j;

import module java.base;
import com.github.kjetilv.uplift.flogs.FlogsContext;
import org.slf4j.spi.MDCAdapter;

/// SLF4J's mapped diagnostic context. The values are kept in [FlogsContext], and flogs adds them to each line
/// that the thread logs. A null value removes the key.
@SuppressWarnings({"unused", "WeakerAccess"})
public final class MDC {

    /// @throws IllegalArgumentException If the key is null
    public static void put(String key, String val) {
        FlogsContext.put(key, val);
    }

    /// @return A [MDCCloseable] that removes the key when closed
    /// @throws IllegalArgumentException If the key is null
    public static MDCCloseable putCloseable(String key, String val) {
        put(key, val);
        return new MDCCloseable(key);
    }

    public static String get(String key) {
        return FlogsContext.get(key);
    }

    public static void remove(String key) {
        FlogsContext.remove(key);
    }

    public static void clear() {
        FlogsContext.clear();
    }

    /// @return A modifiable copy of the context of this thread, in insertion order
    public static Map<String, String> getCopyOfContextMap() {
        return new LinkedHashMap<>(FlogsContext.get());
    }

    /// @param contextMap Context. Null or empty clears the context.
    public static void setContextMap(Map<String, String> contextMap) {
        FlogsContext.set(contextMap);
    }

    public static MDCAdapter getMDCAdapter() {
        return Adapter.INSTANCE;
    }

    private MDC() {
    }

    public static final class MDCCloseable implements Closeable {

        private final String key;

        private MDCCloseable(String key) {
            this.key = key;
        }

        @Override
        public void close() {
            remove(key);
        }
    }

    private enum Adapter implements MDCAdapter {

        INSTANCE;

        @Override
        public void put(String key, String val) {
            MDC.put(key, val);
        }

        @Override
        public String get(String key) {
            return MDC.get(key);
        }

        @Override
        public void remove(String key) {
            MDC.remove(key);
        }

        @Override
        public void clear() {
            MDC.clear();
        }

        @Override
        public Map<String, String> getCopyOfContextMap() {
            return MDC.getCopyOfContextMap();
        }

        @Override
        public void setContextMap(Map<String, String> contextMap) {
            MDC.setContextMap(contextMap);
        }
    }
}
