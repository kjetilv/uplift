package com.github.kjetilv.uplift.kernel;

import module java.base;
import module uplift.flogs;
import org.slf4j.Logger;

public final class EnvLookup {

    private static final Logger log = LoggerFactory.getLogger(EnvLookup.class);

    @SuppressWarnings("SameParameterValue")
    public static String getRequired(String property) {
        return get(property, property, true);
    }

    public static String get(String systemProperty, String environmentVariable) {
        return get(systemProperty, environmentVariable, false);
    }

    public static String get(
        String systemProperty,
        String environmentVariable,
        boolean required
    ) {
        Optional<String> value = cache.computeIfAbsent(
            systemProperty + environmentVariable + required,
            key ->
                resolve(systemProperty, environmentVariable, required)
        );
        return required
            ? value.orElseThrow(missing(systemProperty, environmentVariable))
            : value.orElse(null);
    }

    private EnvLookup() {

    }

    private static final Map<String, Optional<String>> cache = new ConcurrentHashMap<>();

    private static Optional<String> resolve(String systemProperty, String environmentVariable, boolean required) {
        Optional<String> property = systemProperty(systemProperty)
            .or(() ->
                systemProperty(environmentVariable))
            .or(() ->
                environmentVariable(environmentVariable));
        property.ifPresentOrElse(
            propertyValue ->
                log(systemProperty, environmentVariable, propertyValue),
            () ->
                logMissing(systemProperty, environmentVariable, required)
        );
        return property;
    }

    private static Supplier<IllegalStateException> missing(String systemProperty, String environmentVariable) {
        return () ->
            new IllegalStateException("Incomplete environment: " + systemProperty + "/" + environmentVariable);
    }

    private static void logMissing(String systemProperty, String environmentVariable, boolean required) {
        if (required) {
            log.error("Missing: {}/{}", systemProperty, environmentVariable);
        } else {
            log.warn("Missing: {}/{}", systemProperty, environmentVariable);
        }
    }

    private static Optional<String> systemProperty(String systemProperty) {
        return Optional.ofNullable(systemProperty).map(System::getProperty);
    }

    private static Optional<String> environmentVariable(String environmentVariable) {
        return Optional.ofNullable(environmentVariable).map(System::getenv);
    }

    private static void log(String systemProperty, String environmentVariable, String s) {
        int length = s.length();
        int section = Math.min(10, length / 3);
        log.debug(
            "{}/{} -> {}...{} ({} chars)",
            systemProperty,
            environmentVariable,
            s.substring(0, section),
            s.substring(length - section, length),
            length
        );
    }
}
