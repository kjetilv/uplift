package com.github.kjetilv.uplift.plugins.core;

import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * StringTemplate rendering. The delimiters are the same unusual pair the Kotlin version
 * used, because the templates themselves are unchanged.
 */
public final class Templates {

    private static final char OPEN = '⎨';

    private static final char CLOSE = '⎬';

    /**
     * Renders a classpath resource, dropping blank lines. Null values are left unset,
     * which is how the templates express "absent" rather than "empty".
     */
    public static List<String> renderResource(String resource, Map<String, String> parameters) {
        ST st = new ST(loadResource(resource), OPEN, CLOSE);
        parameters.forEach((key, value) -> {
            if (value != null) {
                st.add(key, value);
            }
        });
        return Arrays.stream(st.render().split("\n"))
            .filter(line -> !line.isBlank())
            .toList();
    }

    public static String loadResource(String resource) {
        try (InputStream stream = open(resource)) {
            if (stream == null) {
                throw new IllegalStateException("No template `" + resource + "` in path");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read template `" + resource + "`", e);
        }
    }

    /**
     * Split with a negative limit, so a trailing newline yields a trailing empty element.
     * {@code Files.write} turns that back into a trailing newline, which keeps generated
     * files byte identical to what the Kotlin implementation produced.
     */
    public static List<String> loadResourceLines(String resource) {
        return new ArrayList<>(Arrays.asList(loadResource(resource).split("\n", -1)));
    }

    // The context class loader is what the Kotlin version used, and it is what works
    // inside a Gradle worker. The class loader fallback covers other hosts.
    private static InputStream open(String resource) {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        InputStream stream = context == null ? null : context.getResourceAsStream(resource);
        return stream != null ? stream : Templates.class.getClassLoader().getResourceAsStream(resource);
    }

    private Templates() {
    }
}
