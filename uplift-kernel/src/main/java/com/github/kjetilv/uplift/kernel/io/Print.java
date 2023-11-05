package com.github.kjetilv.uplift.kernel.io;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class Print {

    public static String aboutTime(Instant instant, ZoneId timezone) {
        return instant.atZone(timezone)
            .truncatedTo(ChronoUnit.MINUTES)
            .format(DATE_TIME_FORMATTER);
    }

    public static String semiSecret(String semi) {
        int length = semi.length();
        if (length == 0) {
            return "<empty secret>";
        }
        if (length == 1) {
            return "*";
        }
        if (length < 5) {
            return semi.charAt(0) + "***".substring(0, length - 1);
        }
        int printable = length / 3;
        return semi.substring(0, printable) + "***" + semi.substring(length - printable, printable);
    }

    public static String prettyTrackTime(Duration dur) {
        if (dur.minus(Duration.ofHours(1)).isNegative()) {
            return String.format(
                "%02d:%02d",
                dur.toMinutesPart(),
                dur.toSecondsPart()
            );
        }
        return String.format(
            "%d:%02d:%02d",
            dur.toHoursPart(),
            dur.toMinutesPart(),
            dur.toSecondsPart()
        );
    }

    public static String prettyLongTime(Duration dur) {
        int min = dur.toMinutesPart();
        if (dur.minus(Duration.ofHours(1)).isNegative()) {
            return String.format(
                "%d minutt%s",
                min,
                mulNogender(min, "er")
            );
        }
        int hors = dur.toHoursPart();
        if (dur.minus(Duration.ofDays(1)).isNegative()) {
            if (min == 0) {
                return String.format(
                    "%d time%s",
                    hors,
                    mulNogender(hors, "r")
                );
            }
            return String.format(
                "%d time%s og %d minutt%s",
                hors, mul(hors, "r"),
                min,
                mulNogender(min, "er")
            );
        }
        long days = dur.toDaysPart();
        if (min == 0) {
            if (hors == 0) {
                return String.format(
                    "%d dag%s",
                    days,
                    mul(days, "er")
                );
            }
            return String.format(
                "%d dag%s og %d time%s",
                days, days > 1 ? "er" : "",
                hors, mul(hors, "r")
            );
        }
        if (hors == 0) {
            return String.format(
                "%d dag%s, og %d minutt%s",
                days, days > 1 ? "er" : "",
                min,
                mulNogender(min, "er")
            );
        }
        return String.format(
            "%d dag%s, %d time%s og %d minutt%s",
            days, days > 1 ? "er" : "",
            hors, mul(hors, "r"),
            min, mulNogender(min, "er")
        );
    }

    @SuppressWarnings("MagicNumber")
    public static String bytes(long bytes) {
        if (bytes > 10 * M) {
            return String.format("%dM", bytes / M);
        }
        if (bytes > M) {
            if (bytes % M == 0) {
                return String.format("%dMiB", bytes / M);
            }
            return String.format("%.1fM", bytes * 10 / M / 10.0D);
        }
        if (bytes > K) {
            if (bytes % K == 0) {
                return String.format("%dKiB", bytes / K);
            }
            return String.format("%dk", bytes / K);
        }
        return String.format("%d", bytes);
    }

    private Print() {
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US);

    private static final int K = 1_024;

    private static final int M = K * K;

    private static String mul(int min, String er) {
        return mul((long) min, er);
    }

    private static String mul(long min, String er) {
        return min > 1 ? er : "";
    }

    private static String mulNogender(long min, String er) {
        return min == 1 ? "" : er;
    }
}
