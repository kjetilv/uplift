package com.github.kjetilv.uplift.cdk;

import module aws.cdk.lib;
import module java.base;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.lambda.HttpMethod;

import java.time.Duration;
import java.util.function.Function;

public interface Stacker extends Consumer<Stack> {

    static Stacker lambda(String module) {
        return lambda(null, module);
    }

    static Stacker lambda(String name, String module) {
        return new DefStacker(name, module);
    }

    Stacker withUrl();

    Stacker cors(Function<Cors, Cors> cors);

    Stacker settings(Function<Settings, Settings> settings);

    record Settings(
        Architecture architecture,
        Duration timeout,
        int memoryMb,
        RetentionDays logRetention
    ) {

        public Settings() {
            this(
                Architecture.ARM_64,
                Duration.ofMinutes(1),
                128,
                RetentionDays.ONE_DAY
            );
        }
    }

    record Cors(
        List<HttpMethod> methods,
        List<String> origins,
        List<String> headers,
        Duration maxAge,
        boolean allowCredentials
    ) {

        public Cors() {
            this(
                List.of(HttpMethod.GET),
                List.of("http://localhost:8080"),
                Collections.emptyList(),
                Duration.ofDays(1),
                false
            );
        }

        public Cors(
            List<HttpMethod> methods,
            List<String> origins,
            List<String> headers,
            Duration maxAge,
            boolean allowCredentials
        ) {
            this.methods = sanitize(methods);
            this.origins = sanitize(origins);
            this.headers = sanitize(headers);
            this.maxAge = maxAge == null || maxAge.isNegative() || maxAge.isZero() ? Duration.ofDays(1) : maxAge;
            this.allowCredentials = allowCredentials;
        }

        public Cors maxAge(Duration maxAge) {
            Objects.requireNonNull(maxAge, "maxAge");
            if (maxAge.isNegative() || maxAge.isZero()) {
                throw new IllegalArgumentException("Required >0 duration: " + maxAge);
            }
            return new Cors(methods, origins, headers, maxAge, allowCredentials);
        }

        public Cors allowCredentials(boolean allowCredentials) {
            return new Cors(methods, origins, headers, maxAge, allowCredentials);
        }

        public Cors headers(String... list) {
            return headers(Arrays.asList(list));
        }

        public Cors headers(List<String> list) {
            if (list == null || list.isEmpty()) {
                throw new IllegalArgumentException("No headers: " + list);
            }
            return new Cors(methods, origins, headers, maxAge, allowCredentials);
        }

        public Cors methods(HttpMethod... list) {
            return methods(Arrays.asList(list));
        }

        public Cors methods(List<HttpMethod> list) {
            if (list == null || list.isEmpty()) {
                throw new IllegalArgumentException("No methods: " + list);
            }
            return new Cors(methods, origins, headers, maxAge, allowCredentials);
        }

        public Cors origins(HttpMethod... list) {
            return origins(Arrays.asList(list));
        }

        public Cors origins(List<HttpMethod> list) {
            if (list == null || list.isEmpty()) {
                throw new IllegalArgumentException("No origins: " + list);
            }
            return new Cors(methods, origins, headers, maxAge, allowCredentials);
        }

        private static <T extends Comparable<T>> List<T> sanitize(List<T> ts) {
            return ts == null || ts.isEmpty()
                ? Collections.emptyList()
                : ts.stream()
                    .filter(Objects::nonNull).distinct().sorted()
                    .toList();
        }
    }
}
