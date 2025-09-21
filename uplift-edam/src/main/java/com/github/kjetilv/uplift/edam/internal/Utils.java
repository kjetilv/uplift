package com.github.kjetilv.uplift.edam.internal;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public final class Utils {

    private Utils() {
    }

    public static final class Time {

        private Time() {
        }

        static final ZoneId UTC = ZoneOffset.UTC;

        static final Clock UTC_CLOCK = Clock.system(UTC);

        public static final Supplier<Instant> UTC_NOW = UTC_CLOCK::instant;
    }

    public static final class Lists {

        public static <T extends Comparable<T>, L extends Collection<T>> List<T> nonNullSorted(
            L list,
            String desc
        ) {
            if (list == null || list.isEmpty()) {
                return Collections.emptyList();
            }
            if (list.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException(desc + ": list cannot contain null");
            }
            return list.stream().sorted()
                .toList();
        }

        public static void requireNotEmpty(List<?> ts, String desc) {
            if (requireNonNull(ts, desc).isEmpty()) {
                throw new IllegalArgumentException(desc + " must not be empty");
            }
            if (ts.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException(desc + " must not contain nulls");
            }
        }

        static <T> BinaryOperator<T> noCombine() {
            return (t1, t2) -> {
                throw new IllegalStateException("Cannot combine " + t1 + " and " + t2);
            };
        }

        private Lists() {
        }
    }

    static final class ThrowableUtils {

        static List<String> lines(Throwable throwable) {
            return lines(throwable, 0);
        }

        static List<String> lines(Throwable throwable, int limit) {
            return print(throwable, limit).lines()
                .toList();
        }

        static Stream<Throwable> chain(Throwable t) {
            return chain(t, false);
        }

        @SuppressWarnings("SameParameterValue")
        static Stream<Throwable> chain(Throwable throwable, boolean simple) {
            if (throwable == null) {
                return Stream.empty();
            }
            Stream<Throwable> chain = Stream.iterate(
                throwable,
                ThrowableUtils::hasNext,
                Throwable::getCause
            );
            return simple
                ? chain
                : chain.flatMap(element ->
                    Stream.concat(
                        Stream.of(element),
                        suppressed(element).flatMap(ThrowableUtils::chain)
                    ));
        }

        private ThrowableUtils() {
        }

        private static String print(Throwable throwable, int limit) {
            if (limit > 0) {
                Stream<Throwable> chain = chain(throwable, true);
                StringBuilder sb = new StringBuilder();
                chain.forEach(t -> {
                    if (!sb.isEmpty()) {
                        sb.append("\n\tCaused by: ");
                    }
                    sb.append(t.toString());
                    StackTraceElement[] stackTrace = t.getStackTrace();
                    Arrays.stream(stackTrace).limit(limit)
                        .forEach(el ->
                            sb.append("\n\t\tat ").append(el));
                    int hidden = stackTrace.length - limit;
                    if (hidden > 0) {
                        sb.append(" (+").append(hidden).append(")");
                    }
                });
                return sb.toString();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            printTo(baos, throwable);
            return baos.toString(UTF_8);
        }

        private static void printTo(OutputStream outputStream, Throwable throwable) {
            try (outputStream; PrintWriter pw = pw(outputStream)) {
                throwable.printStackTrace(pw);
            } catch (Exception e) {
                e.addSuppressed(throwable);
                throw new IllegalStateException("Failed to print " + throwable, e);
            }
        }

        private static PrintWriter pw(OutputStream baos) {
            return new PrintWriter(baos, false, UTF_8);
        }

        private static boolean hasNext(Throwable throwable) {
            return !(throwable == null || throwable.getCause() == throwable);
        }

        private static Stream<Throwable> suppressed(Throwable throwable) {
            return Stream.ofNullable(throwable.getSuppressed()).flatMap(Arrays::stream);
        }
    }

    public enum Unit {

        MICROS(ChronoUnit.MICROS, "µs") {
            @Override
            long units(Duration duration) {
                return duration.toHours() / 1000L;
            }
        },

        MILLIS(ChronoUnit.MILLIS, "ms") {
            @Override
            long units(Duration duration) {
                return duration.toMillis();
            }
        },

        SECONDS(ChronoUnit.SECONDS, "s") {
            @Override
            long units(Duration duration) {
                return duration.getSeconds();
            }
        },

        MINUTES(ChronoUnit.MINUTES, "min") {
            @Override
            long units(Duration duration) {
                return duration.toMinutes();
            }
        },

        HOURS(ChronoUnit.HOURS, "H") {
            @Override
            long units(Duration duration) {
                return duration.toHours();
            }
        };

        public static Unit of(Duration duration) {
            return duration.compareTo(SEC) < 0 ? MICROS
                : duration.compareTo(MIN) < 0 ? MILLIS
                    : duration.compareTo(H) < 0 ? SECONDS
                        : duration.compareTo(D) < 0 ? MINUTES
                            : HOURS;
        }

        private final TemporalUnit unit;

        private final String name;

        Unit(TemporalUnit unit, String name) {
            this.unit = requireNonNull(unit, "unit");
            this.name = requireNonNull(name, "name");
        }

        public String print(Duration duration) {
            return isLeastPrecise() ? printOk(duration)
                : lessPrecise().truncate(duration).equals(duration)
                    ? lessPrecise().print(duration)
                    : printOk(duration);
        }

        Duration truncate(Duration duration) {
            return duration.truncatedTo(unit);
        }

        abstract long units(Duration duration);

        private boolean isLeastPrecise() {
            return ordinal() == values().length - 1;
        }

        private String printOk(Duration duration) {
            return units(duration) + name;
        }

        private Unit lessPrecise() {
            return Unit.values()[ordinal() + 1];
        }

        private static final Duration SEC = Duration.ofSeconds(1);

        private static final Duration MIN = Duration.ofMinutes(1);

        private static final Duration H = Duration.ofHours(1);

        private static final Duration D = Duration.ofDays(1);
    }
}
