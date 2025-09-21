package com.github.kjetilv.uplift.kernel.aws;

import module java.base;
import module uplift.util;

final class AwsLookup {

    static Optional<AwsAuth> get(String profile) {
        if (profile == null || profile.isBlank()) {
            return Optional.empty();
        }
        Path credentials = Path.of(System.getProperty("user.home"))
            .resolve(".aws")
            .resolve("credentials");
        try {
            try (
                Stream<String> lines = Files.lines(credentials)
                    .dropWhile(line ->
                        profileStart(profile, line))
                    .skip(1)
                    .takeWhile(AwsLookup::isProfile)
                    .map(String::trim)
            ) {
                Map<String, String> auths = Maps.indexBy(lines.toList(), line ->
                    line.substring(0, line.lastIndexOf(' ')));
                return Maps.get(auths, key -> key.startsWith("aws_access_key_id")).findFirst().flatMap(key ->
                    Maps.get(auths, accessKey -> accessKey.startsWith("aws_secret_access_key")).findFirst().map(value ->
                        new AwsAuth(key, value)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AwsLookup() {
    }

    private static boolean isProfile(String line) {
        return !line.startsWith("[") && !line.isBlank();
    }

    private static boolean profileStart(String profile, String line) {
        return !line.startsWith("[" + profile + "]");
    }
}
