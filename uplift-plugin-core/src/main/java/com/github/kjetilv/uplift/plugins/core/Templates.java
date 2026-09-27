package com.github.kjetilv.uplift.plugins.core;

import module java.base;

/**
 * Template rendering. The delimiters are the same unusual pair the Kotlin version
 * used, because the templates themselves are unchanged.
 */
public final class Templates {

    /**
     * Renders a classpath resource, dropping blank lines. Null values are left unset,
     * which is how the templates express "absent" rather than "empty".
     */
    public static List<String> renderResource(
        String resource,
        Map<String, String> parameters
    ) {
        var template = loadResource(resource);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null) {
                template = template.replaceAll(
                    "⎨%s⎬".formatted(key),
                    value
                );
            }
        }
        return Arrays.stream(template.split("\n"))
            .filter(line -> !line.isBlank())
            .toList();
    }

    public static String loadResource(String resource) {
        return open(resource)
            .map(stream ->
                readResoruce(resource, stream))
            .orElseThrow(() ->
                new IllegalStateException("Failed to locate template " + resource));
    }

    /**
     * Split with a negative limit, so a trailing newline yields a trailing empty element.
     * {@code Files.write} turns that back into a trailing newline, which keeps generated
     * files byte identical to what the Kotlin implementation produced.
     */
    public static List<String> loadResourceLines(String resource) {
        return new ArrayList<>(Arrays.asList(loadResource(resource).split("\n", -1)));
    }

    private Templates() {
    }

    private static String readResoruce(String resource, InputStream stream) {
        try {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read template " + resource, e);
        }
    }

    private static Optional<InputStream> open(String resource) {
        return Optional.ofNullable(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(resource))
            .or(() ->
                Optional.ofNullable(
                    Templates.class.getClassLoader().getResourceAsStream(resource)));
    }
}
