package com.github.kjetilv.uplift.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class EnvLookup {

    private static final Logger log = LoggerFactory.getLogger(EnvLookup.class);

    @SuppressWarnings("SameParameterValue")
    public static String get(String property) {
        return getOptional(property, property, true)
            .orElseThrow(missing(property, property));
    }

    public static Optional<String> maybeGet(String property) {
        return getOptional(property, property, false);
    }

    public static String get(String systemProperty, String environmentVariable) {
        return getOptional(systemProperty, environmentVariable, false).orElse(null);
    }

    private EnvLookup() {
    }

    private static final Map<String, Optional<String>> cache = new ConcurrentHashMap<>();

    private static Optional<String> getOptional(
        String systemProperty,
        String environmentVariable,
        boolean required
    ) {
        return cache.computeIfAbsent(
            String.join(
                "-",
                systemProperty,
                environmentVariable,
                required ? "required" : "optional"
            ),
            _ ->
                resolve(systemProperty, environmentVariable, required)
        );
    }

    private static Optional<String> resolve(
        String systemProperty,
        String environmentVariable,
        boolean required
    ) {
        var property = systemProperty(systemProperty)
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
            log.debug("Not found in environment {}/{}", systemProperty, environmentVariable);
        }
    }

    private static Optional<String> systemProperty(String systemProperty) {
        return Optional.ofNullable(systemProperty)
            .map(System::getProperty);
    }

    private static Optional<String> environmentVariable(String environmentVariable) {
        return Optional.ofNullable(environmentVariable)
            .map(System::getenv);
    }

    private static void log(String systemProperty, String environmentVariable, String s) {
        var length = s.length();
        var section = Math.min(10, length / 3);
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
