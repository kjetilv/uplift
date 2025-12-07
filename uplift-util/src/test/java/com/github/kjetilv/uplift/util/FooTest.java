package com.github.kjetilv.uplift.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Gatherer;
import java.util.stream.IntStream;

import static java.time.Month.*;
import static org.assertj.core.api.Assertions.assertThat;

public class FooTest {

    @Test
    void nothing() {
        var actual = validOn(
            List.of(),
            date(JUNE, 28)
        );
        assertThat(actual).isEmpty();
    }

    @Test
    void pickFirstBefore() {
        assertThat(validOn(
            monthsRange(JANUARY, OCTOBER),
            date(JUNE, 28)
        )).containsExactlyElementsOf(monthsRange(JUNE, OCTOBER));
    }

    @Test
    void pickAll() {
        assertThat(validOn(
            monthsRange(JUNE, NOVEMBER),
            date(FEBRUARY, 17)
        )).containsExactlyElementsOf(monthsRange(JUNE, NOVEMBER));
    }

    @Test
    void pickLast() {
        assertThat(validOn(
            monthsRange(JANUARY, APRIL),
            date(JUNE, 28)
        )).containsExactly(APRIL);
    }

    private List<Month> validOn(List<Month> months, LocalDate date) {
        return validOn(date, months, TO_DATE);
    }

    private <T> List<T> validOn(LocalDate targetDate, List<T> ts, Function<T, LocalDate> dater) {
        return ts.stream()
            .sorted(Comparator.comparing(dater))
            .gather(validOn(targetDate, dater))
            .toList();
    }

    private static final Function<Month, LocalDate> TO_DATE = month -> LocalDate.of(2025, month, 1);

    private static <T> Gatherer<T, ?, T> validOn(
        LocalDate targetDate,
        Function<T, LocalDate> dater
    ) {
        return Gatherer.<T, AtomicReference<T>, T>ofSequential(
            AtomicReference::new,
            (state, current, downstream) -> {
                var previous = state.getAndSet(current);
                if (previous != null && dater.apply(current).isAfter(targetDate)) {
                    downstream.push(previous);
                }
                return true;
            },
            (state, downstream) -> {
                var tail = state.getAndSet(null);
                if (tail != null) {
                    downstream.push(tail);
                }
            }
        );
    }

    private static List<Month> monthsRange(Month from, Month to) {
        return IntStream.range(from.getValue(), to.getValue() + 1).mapToObj(Month::of)
            .toList();
    }

    private static LocalDate date(Month month, int day) {
        return LocalDate.of(2025, month.getValue(), day);
    }
}
