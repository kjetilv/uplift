package org.apache.logging.log4j;

import module java.base;

/// Log4j levels. A smaller [#intLevel()] is more specific. Flogs maps a level to the nearest flogs level:
/// FATAL and ERROR to ERROR, and the others to the level with the same name. A custom level maps by its
/// [#intLevel()]. OFF is never logged.
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Level implements Comparable<Level> {

    private static final Map<String, Level> LEVELS = new ConcurrentHashMap<>();

    public static final Level OFF = new Level("OFF", 0);

    public static final Level FATAL = new Level("FATAL", 100);

    public static final Level ERROR = new Level("ERROR", 200);

    public static final Level WARN = new Level("WARN", 300);

    public static final Level INFO = new Level("INFO", 400);

    public static final Level DEBUG = new Level("DEBUG", 500);

    public static final Level TRACE = new Level("TRACE", 600);

    public static final Level ALL = new Level("ALL", Integer.MAX_VALUE);

    public static final String CATEGORY = "Level";

    /// @return The level with the name, created with the int level if it does not exist
    /// @throws IllegalArgumentException If the name is null or blank, or the int level is negative
    public static Level forName(String name, int intValue) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Level name cannot be null or blank");
        }
        if (intValue < 0) {
            throw new IllegalArgumentException("Level int value cannot be negative: " + intValue);
        }
        return LEVELS.computeIfAbsent(key(name), _ -> new Level(name.trim(), intValue, false));
    }

    /// @return The level with the name, or null if it does not exist
    public static Level getLevel(String name) {
        return name == null || name.isBlank() ? null : LEVELS.get(key(name));
    }

    /// @return The level with the name, or DEBUG if it does not exist
    public static Level toLevel(String name) {
        return toLevel(name, DEBUG);
    }

    /// @return The level with the name, or the default level if it does not exist
    public static Level toLevel(String name, Level defaultLevel) {
        var level = getLevel(name);
        return level == null ? defaultLevel : level;
    }

    /// @return The level with the name
    /// @throws IllegalArgumentException If the level does not exist
    public static Level valueOf(String name) {
        var level = getLevel(Objects.requireNonNull(name, "name"));
        if (level == null) {
            throw new IllegalArgumentException("Unknown level: " + name);
        }
        return level;
    }

    /// @return All levels, from the most specific to the least specific
    public static Level[] values() {
        return LEVELS.values().stream().sorted().toArray(Level[]::new);
    }

    private final String name;

    private final int intLevel;

    private Level(String name, int intLevel) {
        this(name, intLevel, true);
    }

    private Level(String name, int intLevel, boolean register) {
        this.name = name;
        this.intLevel = intLevel;
        if (register) {
            LEVELS.put(key(name), this);
        }
    }

    public String name() {
        return name;
    }

    public int intLevel() {
        return intLevel;
    }

    /// @return True if this level is between the two levels, both included
    public boolean isInRange(Level minLevel, Level maxLevel) {
        return intLevel >= minLevel.intLevel && intLevel <= maxLevel.intLevel;
    }

    /// @return True if this level is the same as, or less specific than, the other level
    public boolean isLessSpecificThan(Level level) {
        return intLevel >= level.intLevel;
    }

    /// @return True if this level is the same as, or more specific than, the other level
    public boolean isMoreSpecificThan(Level level) {
        return intLevel <= level.intLevel;
    }

    @Override
    public int compareTo(Level other) {
        return Integer.compare(intLevel, other.intLevel);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Level level && name.equals(level.name) && intLevel == level.intLevel;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    private static String key(String name) {
        return name.trim().toUpperCase(Locale.ROOT);
    }
}
