package com.github.kjetilv.uplift.edam;

import module java.base;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.edam.patterns.PatternOccurrence;
import com.github.kjetilv.uplift.hash.HashKind;

import static com.github.kjetilv.uplift.edam.Analysis.Multiple;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@FunctionalInterface
public interface Handler<T, P extends Info<T, K>, K extends HashKind<K>> {

    default <R> R map(T item, Function<Handling<T, P, K>, R> action) {
        return action.apply(handling(item));
    }

    default Handler.With<T, P, K> handle(T item) {
        return new With<>() {

            @Override
            public <R> R with(BiFunction<Analysis<K>, P, R> processor) {
                var handling = handling(item);
                return processor.apply(handling.analysis(), handling.payload());
            }
        };
    }

    Handling<T, P, K> handling(T item);

    private static <T, P extends Info<T, K>, K extends HashKind<K>> void none(
        Results<T, K> results,
        Handling<T, P, K> handling
    ) {
        results.singleOccurrence(handling.payload().source());
    }

    private static <T, P extends Info<T, K>, K extends HashKind<K>> void simple(
        Results<T, K> results,
        Handling<T, P, K> handling,
        List<Occurrence<K>> occurrences
    ) {
        results.multipleOccurrences(handling.payload().source(), occurrences);
    }

    private static <T, P extends Info<T, K>, K extends HashKind<K>> void simple(
        Results<T, K> results,
        Handling<T, P, K> handling,
        Multiple<K> multiple
    ) {
        multiple.simpleMatch()
            .ifPresent(match ->
                results.multipleOccurrences(
                    handling.payload().source(),
                    match.occurrences()
                ));
    }

    private static <T, P extends Info<T, K>, K extends HashKind<K>> void combined(
        Results<T, K> results,
        Handling<T, P, K> handling,
        Multiple<K> multiple
    ) {
        multiple.combinedMatches()
            .forEach(match ->
                results.patternOccurred(handling.payload().source(), match.occurrences()));
    }

    @FunctionalInterface
    interface With<T, P extends Info<T, K>, K extends HashKind<K>> {

        default void with(Consumer<Analysis<K>> consumer) {
            with((an, p) -> {
                consumer.accept(an);
            });
        }

        default void with(BiConsumer<Analysis<K>, P> processor) {
            with((kAnalysis, p) -> {
                processor.accept(kAnalysis, p);
                return null;
            });
        }

        default <R> R with(Function<Analysis<K>, R> processor) {
            return with((analysis, p) -> {
                return processor.apply(analysis);
            });
        }

        <R> R with(BiFunction<Analysis<K>, P, R> processor);
    }

    interface Results<T, K extends HashKind<K>> {

        void singleOccurrence(T payload);

        void multipleOccurrences(T source, List<Occurrence<K>> list);

        void patternOccurred(T source, List<PatternOccurrence<K>> patternOccurrences);
    }
}
