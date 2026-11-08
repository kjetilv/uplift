package com.github.kjetilv.uplift.edam;

import module java.base;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.edam.patterns.PatternOccurrence;
import com.github.kjetilv.uplift.hash.HashKind;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@FunctionalInterface
public interface Handler<T, P extends Info<T, H>, H extends HashKind<H>> {

    default <R> R map(T item, Function<Handling<T, P, H>, R> action) {
        return action.apply(handling(item));
    }

    default Handler.With<T, P, H> handle(T item) {
        return new With<>() {

            @Override
            public <R> R with(BiFunction<Analysis<H>, P, R> processor) {
                var handling = handling(item);
                return processor.apply(handling.analysis(), handling.payload());
            }
        };
    }

    Handling<T, P, H> handling(T item);

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
