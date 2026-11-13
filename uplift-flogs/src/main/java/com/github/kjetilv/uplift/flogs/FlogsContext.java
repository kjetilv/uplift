package com.github.kjetilv.uplift.flogs;

import module java.base;

/// The log context of each thread: keys and values that flogs adds to each line that the thread logs.
/// [org.slf4j.MDC] keeps its values here.
///
/// The context of a thread is an unmodifiable map, in insertion order. Each change replaces the map, so a log
/// line reads the context without a copy. A null value removes the key.
///
/// The context stays on its thread. Work that moves to an other thread, such as a [CompletableFuture] stage
/// or an executor task, does not get it. Clear the context when a thread completes a unit of work, if the
/// thread then does other work.
public final class FlogsContext {

    /// @param key   Key, not null
    /// @param value Value. Null removes the key.
    /// @throws IllegalArgumentException If the key is null
    public static void put(String key, String value) {
        requireKey(key);
        if (value == null) {
            remove(key);
            return;
        }
        var current = get();
        if (!value.equals(current.get(key))) {
            var updated = new LinkedHashMap<>(current);
            updated.put(key, value);
            CONTEXT.set(Collections.unmodifiableMap(updated));
        }
    }

    /// @param key Key, may be null
    /// @return The value, or null if the key is null or not in the context
    public static String get(String key) {
        return key == null ? null : get().get(key);
    }

    /// @param key Key, may be null
    public static void remove(String key) {
        var current = get();
        if (key == null || !current.containsKey(key)) {
            return;
        }
        if (current.size() == 1) {
            clear();
            return;
        }
        var updated = new LinkedHashMap<>(current);
        updated.remove(key);
        CONTEXT.set(Collections.unmodifiableMap(updated));
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /// Replaces the context of this thread.
    ///
    /// @param context Context. Null or empty clears the context. Keys with null values are left out.
    /// @throws IllegalArgumentException If a key is null
    public static void set(Map<String, String> context) {
        var copy = copy(context);
        if (copy.isEmpty()) {
            clear();
        } else {
            CONTEXT.set(copy);
        }
    }

    /// @return The context of this thread, unmodifiable. Empty if no context is set.
    public static Map<String, String> get() {
        var context = CONTEXT.get();
        return context == null ? Map.of() : context;
    }

    private FlogsContext() {
    }

    private static final ThreadLocal<Map<String, String>> CONTEXT = new ThreadLocal<>();

    private static Map<String, String> copy(Map<String, String> context) {
        if (context == null || context.isEmpty()) {
            return Map.of();
        }
        var copy = context.entrySet()
            .stream()
            .filter(entry -> requireKey(entry.getKey()) && entry.getValue() != null)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (_, last) -> last,
                LinkedHashMap::new
            ));
        return copy.isEmpty() ? Map.of() : Collections.unmodifiableMap(copy);
    }

    private static boolean requireKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        return true;
    }
}
