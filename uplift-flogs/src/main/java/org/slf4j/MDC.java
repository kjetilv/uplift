package org.slf4j;

import module java.base;
import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.FlogsContext;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.slf4j.spi.MDCAdapter;

/// SLF4J's mapped diagnostic context, as a read-only view of [FlogsContext]. Flogs keeps the context in a
/// [ScopedValue], which cannot change. Use [FlogsContext#where(String, String)] to set it.
///
/// [#get(String)] and [#getCopyOfContextMap()] read the context in effect. The operations that change the
/// context ([#put(String, String)], [#putCloseable(String, String)], [#remove(String)], [#clear()] and
/// [#setContextMap(Map)]) have no effect. Each of them logs a warning, one time for each operation and calling
/// class.
@SuppressWarnings({"unused", "WeakerAccess"})
public final class MDC {

    /// Has no effect. Logs a warning.
    public static void put(String key, String val) {
        ignored("put", key);
    }

    /// Has no effect. Logs a warning.
    ///
    /// @return A [MDCCloseable] that has no effect when closed
    public static MDCCloseable putCloseable(String key, String val) {
        ignored("putCloseable", key);
        return NO_OP_CLOSEABLE;
    }

    public static String get(String key) {
        return FlogsContext.get(key);
    }

    /// Has no effect. Logs a warning.
    public static void remove(String key) {
        ignored("remove", key);
    }

    /// Has no effect. Logs a warning.
    public static void clear() {
        ignored("clear", null);
    }

    /// @return A modifiable copy of the context in effect, in insertion order
    public static Map<String, String> getCopyOfContextMap() {
        return new LinkedHashMap<>(FlogsContext.get());
    }

    /// Has no effect. Logs a warning.
    public static void setContextMap(Map<String, String> contextMap) {
        ignored("setContextMap", null);
    }

    public static MDCAdapter getMDCAdapter() {
        return Adapter.INSTANCE;
    }

    private MDC() {
    }

    private static final MDCCloseable NO_OP_CLOSEABLE = new MDCCloseable();

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private static final StackWalker WALKER = StackWalker.getInstance();

    private static final String MDC_CLASS = MDC.class.getName();

    private static void ignored(String operation, String key) {
        var caller = caller();
        if (WARNED.add(operation + " " + caller)) {
            Flogs.get(MDC.class).log(
                LogLevel.WARN,
                "MDC.{}({}) from {} has no effect: the log context can only be set with FlogsContext.where(...)",
                operation,
                key == null ? "" : key,
                caller
            );
        }
    }

    /// @return The first class in the stack that is not this class or one of its nested classes
    private static String caller() {
        return WALKER.walk(frames -> frames
            .map(StackWalker.StackFrame::getClassName)
            .filter(name -> !(name.equals(MDC_CLASS) || name.startsWith(MDC_CLASS + "$")))
            .findFirst()
            .orElse("unknown"));
    }

    public static final class MDCCloseable implements Closeable {

        private MDCCloseable() {
        }

        @Override
        public void close() {
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
