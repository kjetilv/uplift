package com.github.kjetilv.uplift.flogs.internal;

import module java.base;
import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.LogLevel;

/// Warns about calls that try to change the log context through a facade, such as SLF4J's `MDC` or Log4j's
/// `ThreadContext`. The log context is a [ScopedValue], so these calls have no effect. Each warning is logged
/// one time for each facade, operation and calling class.
///
/// This package is not exported.
public final class IgnoredContextChanges {

    /// @param facade    Facade class that received the call
    /// @param operation Name of the operation
    /// @param key       Key, may be null
    public static void report(Class<?> facade, String operation, String key) {
        var caller = caller(facade.getName());
        if (WARNED.add(facade.getName() + " " + operation + " " + caller)) {
            Flogs.get(facade).log(
                LogLevel.WARN,
                "{}.{}({}) from {} has no effect: the log context can only be set with FlogsContext.where(...)",
                facade.getSimpleName(),
                operation,
                key == null ? "" : key,
                caller
            );
        }
    }

    private IgnoredContextChanges() {
    }

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private static final StackWalker WALKER = StackWalker.getInstance();

    private static final String THIS_CLASS = IgnoredContextChanges.class.getName();

    /// @return The first class in the stack that is not this class, the facade or a nested class of the facade
    private static String caller(String facade) {
        return WALKER.walk(frames -> frames
            .map(StackWalker.StackFrame::getClassName)
            .filter(name -> !(
                name.equals(THIS_CLASS) ||
                name.equals(facade) ||
                name.startsWith(facade + "$")
            ))
            .findFirst()
            .orElse("unknown"));
    }
}
