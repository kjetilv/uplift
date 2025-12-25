package com.github.kjetilv.uplift.kernel.aws;

import com.github.kjetilv.uplift.util.Maps;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

final class AwsLookup {

    static Optional<AwsAuth> get(String profile) {
        var credentials = Path.of(System.getProperty("user.home"))
            .resolve(".aws")
            .resolve("credentials");
        try (
            var lines = Files.lines(credentials)
                .map(String::trim)
                .dropWhile(notStartOf(profile))
                .skip(1)
                .takeWhile(AwsLookup::isProfile)
        ) {
            var auths = Maps.indexBy(
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
                            new AwsAuth(valueOf(key), valueOf(value))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AwsLookup() {
    }

    private static final Pattern SPLIT = Pattern.compile("\\s*=\\s*");

    private static String valueOf(String value) {
        var pair = SPLIT.split(value, 2);
        if (pair.length == 2) {
            return pair[1];
        }
        throw new IllegalArgumentException("Not a good pair: " + value);
    }

    private static Predicate<String> notStartOf(String profile) {
        var prefix = profile == null || profile.isBlank() ? "[default]" : "[" + profile + "]";
        return line -> !line.startsWith(prefix);
    }

    private static boolean isProfile(String line) {
        return !line.startsWith("[") && !line.isBlank();
    }
}
