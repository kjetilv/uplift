package com.github.kjetilv.uplift.lambda;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Params {

    static Optional<String> param(String original, String pattern, String name) {
        return Optional.of(replace(unstarred(pattern), name))
            .map(Pattern::compile)
            .map(regex -> regex.matcher(original))
            .filter(Matcher::matches)
            .map(matcher ->
                matcher.group(1));
    }

    private Params() {
    }

    private static final String PATTERN = "([\\w\\-]*)";

    private static String unstarred(String pattern) {
        return pattern.startsWith("*") ? ".*" + pattern.substring(1) : pattern;
    }

    private static String replace(String pattern, String name) {
        return pattern.replace("{" + name + "}", PATTERN);
    }
}
