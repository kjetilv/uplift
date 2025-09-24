package com.github.kjetilv.uplift.kernel.aws;

import module java.base;
import module uplift.util;

final class AwsLookup {

    static Optional<AwsAuth> get(String profile) {
        Path credentials = Path.of(System.getProperty("user.home"))
            .resolve(".aws")
            .resolve("credentials");
        try (
            Stream<String> lines = Files.lines(credentials)
                .map(String::trim)
                .dropWhile(notStartOf(profile))
                .skip(1)
                .takeWhile(AwsLookup::isProfile)
        ) {
            Map<String, String> auths = Maps.indexBy(
                lines.toList(),
                line ->
                    line.substring(0, line.lastIndexOf(' '))
            );
            return Maps.get(
                    auths,
                    key ->
                        key.startsWith("aws_access_key_id")
                )
                .findFirst().flatMap(key ->
                    Maps.get(
                            auths,
                            accessKey ->
                                accessKey.startsWith("aws_secret_access_key")
                        )
                        .findFirst()
                        .map(value ->
                            new AwsAuth(key, value)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AwsLookup() {
    }

    private static Predicate<String> notStartOf(String profile) {
        String prefix = profile == null || profile.isBlank() ? "[default]" : "[" + profile + "]";
        return line -> !line.startsWith(prefix);
    }

    private static boolean isProfile(String line) {
        return !line.startsWith("[") && !line.isBlank();
    }
}
