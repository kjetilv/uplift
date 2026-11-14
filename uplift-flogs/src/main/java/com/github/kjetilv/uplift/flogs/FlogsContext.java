package com.github.kjetilv.uplift.flogs;

import module java.base;

/// The log context: keys and values that flogs adds to each line logged inside a scope. The context is a
/// [ScopedValue]. It is bound for the time that [Scope#run(Runnable)] or [Scope#call(ScopedValue.CallableOp)]
/// executes, and it cannot change in that time.
///
/// ```
/// FlogsContext.where("req", id).run(() -> log.info("Handling")); // ... Handling {req=id} ...
/// ```
///
/// A nested scope adds its keys to the keys of the outer scope, and replaces the values of keys that are
/// in both. A null value removes the key in the nested scope. The context is an unmodifiable map, in
/// insertion order.
///
/// Subtasks of a `StructuredTaskScope` (preview feature) get the context of the scope that forks them.
/// Other work that moves to an other thread, such as a [CompletableFuture] stage or an executor task, does
/// not get it.
///
/// [org.slf4j.MDC] can read the context, but it cannot change it.
public final class FlogsContext {

    /// @param key   Key, not null
    /// @param value Value. Null removes the key.
    /// @return A scope with this key and value, in addition to the context in effect when it runs
    /// @throws IllegalArgumentException If the key is null
    public static Scope where(String key, String value) {
        return Scope.EMPTY.where(key, value);
    }

    /// @param context Keys and values. Null values remove keys. May be null.
    /// @return A scope with these keys and values, in addition to the context in effect when it runs
    /// @throws IllegalArgumentException If a key is null
    public static Scope where(Map<String, String> context) {
        return Scope.EMPTY.where(context);
    }

    /// @param key Key, may be null
    /// @return The value, or null if the key is null or not in the context
    public static String get(String key) {
        return key == null ? null : get().get(key);
    }

    /// @return The context in effect, unmodifiable. Empty if no context is bound.
    public static Map<String, String> get() {
        return CONTEXT.orElse(Map.of());
    }

    private FlogsContext() {
    }

    private static final ScopedValue<Map<String, String>> CONTEXT = ScopedValue.newInstance();

    /// Keys and values to bind, with the context in effect, when the scope runs.
    ///
    /// @param changes Keys and values, in insertion order. A null value removes the key.
    public record Scope(Map<String, String> changes) {

        public Scope {
            changes = Collections.unmodifiableMap(new LinkedHashMap<>(changes));
        }

        /// @param key   Key, not null
        /// @param value Value. Null removes the key.
        /// @return A scope with this key and value added
        /// @throws IllegalArgumentException If the key is null
        public Scope where(String key, String value) {
            return where(Collections.singletonMap(validKey(key, value), value));
        }

        /// @param context Keys and values. Null values remove keys. May be null.
        /// @return A scope with these keys and values added
        /// @throws IllegalArgumentException If a key is null
        public Scope where(Map<String, String> context) {
            if (context == null || context.isEmpty()) {
                return this;
            }
            var combined = new LinkedHashMap<>(changes);
            context.forEach((key, value) ->
                combined.put(validKey(key, value), value));
            return new Scope(combined);
        }

        public void run(Runnable op) {
            ScopedValue.where(CONTEXT, merged()).run(op);
        }

        public <T, X extends Throwable> T call(ScopedValue.CallableOp<T, X> op) throws X {
            return ScopedValue.where(CONTEXT, merged()).call(op);
        }

        private static final Scope EMPTY = new Scope(Map.of());

        private Map<String, String> merged() {
            var merged = new LinkedHashMap<>(get());
            changes.forEach((key, value) -> {
                if (value == null) {
                    merged.remove(key);
                } else {
                    merged.put(key, value);
                }
            });
            return merged.isEmpty() ? Map.of() : Collections.unmodifiableMap(merged);
        }
    }

    private static String validKey(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null -> " + value);
        }
        return key;
    }
}
