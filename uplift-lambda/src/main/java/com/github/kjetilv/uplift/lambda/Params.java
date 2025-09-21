package com.github.kjetilv.uplift.lambda;

import module java.base;

final class Params {

    static Optional<String> param(String original, String pattern, String name) {
        return Optional.of(replace(unstarred(pattern), name))
            .map(Pattern::compile)
            .map(regex -> regex.matcher(original))
            .filter(Matcher::matches)
            .map(matcher ->
                matcher.group(1));
    }

    static List<String> params(String original, String pattern, String... names) {
        String regex = Arrays.stream(names).reduce(pattern, Params::replace);
        return Optional.of(Pattern.compile(regex).matcher(original))
            .filter(Matcher::matches)
            .map(matcher ->
                IntStream.range(0, names.length).mapToObj(i -> i + 1).map(matcher::group).toList())
            .orElseGet(Collections::emptyList);
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
