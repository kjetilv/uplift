package org.apache.logging.log4j;

import module java.base;
import com.github.kjetilv.uplift.flogs.FlogsContext;
import com.github.kjetilv.uplift.flogs.internal.IgnoredContextChanges;

/// Log4j's thread context, as a read-only view of [FlogsContext]. Flogs keeps the context in a [ScopedValue],
/// which cannot change. Use [FlogsContext#where(String, String)] to set it.
///
/// The map operations that read ([#get(String)], [#containsKey(String)], [#getContext()],
/// [#getImmutableContext()] and [#isEmpty()]) read the context in effect. The operations that change the
/// map or the stack have no effect. Each of them logs a warning, one time for each operation and calling class.
/// The stack is always empty.
@SuppressWarnings({"unused", "WeakerAccess"})
public final class ThreadContext {

    public static final Map<String, String> EMPTY_MAP = Map.of();

    /// Has no effect. Logs a warning.
    public static void put(String key, String value) {
        ignored("put", key);
    }

    /// Has no effect. Logs a warning.
    public static void putIfNull(String key, String value) {
        ignored("putIfNull", key);
    }

    /// Has no effect. Logs a warning.
    public static void putAll(Map<String, String> map) {
        ignored("putAll", null);
    }

    public static String get(String key) {
        return FlogsContext.get(key);
    }

    /// Has no effect. Logs a warning.
    public static void remove(String key) {
        ignored("remove", key);
    }

    /// Has no effect. Logs a warning.
    public static void removeAll(Iterable<String> keys) {
        ignored("removeAll", null);
    }

    /// Has no effect. Logs a warning.
    public static void clearMap() {
        ignored("clearMap", null);
    }

    /// Has no effect. Logs a warning.
    public static void clearAll() {
        ignored("clearAll", null);
    }

    public static boolean containsKey(String key) {
        return key != null && FlogsContext.get().containsKey(key);
    }

    /// @return A modifiable copy of the context in effect, in insertion order
    public static Map<String, String> getContext() {
        return new LinkedHashMap<>(FlogsContext.get());
    }

    /// @return The context in effect, unmodifiable
    public static Map<String, String> getImmutableContext() {
        return FlogsContext.get();
    }

    public static boolean isEmpty() {
        return FlogsContext.get().isEmpty();
    }

    /// Has no effect. Logs a warning.
    public static void clearStack() {
        ignored("clearStack", null);
    }

    /// @return 0. The stack is always empty.
    public static int getDepth() {
        return 0;
    }

    /// Has no effect. Logs a warning.
    ///
    /// @return An empty string. The stack is always empty.
    public static String pop() {
        ignored("pop", null);
        return "";
    }

    /// @return An empty string. The stack is always empty.
    public static String peek() {
        return "";
    }

    /// Has no effect. Logs a warning.
    public static void push(String message) {
        ignored("push", null);
    }

    /// Has no effect. Logs a warning.
    public static void push(String message, Object... args) {
        ignored("push", null);
    }

    /// Has no effect. Logs a warning.
    public static void removeStack() {
        ignored("removeStack", null);
    }

    /// Has no effect. Logs a warning.
    public static void trim(int depth) {
        ignored("trim", null);
    }

    /// Has no effect. Logs a warning.
    public static void setStack(Collection<String> stack) {
        ignored("setStack", null);
    }

    /// Has no effect. Flogs needs no initialization.
    public static void init() {
    }

    private ThreadContext() {
    }

    private static void ignored(String operation, String key) {
        IgnoredContextChanges.report(ThreadContext.class, operation, key);
    }
}
