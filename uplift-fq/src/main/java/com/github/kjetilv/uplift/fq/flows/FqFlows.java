package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface FqFlows<T> {

    static <T> Builder<T> builder(Name name, Fqs<T> fqs) {
        return new DefaultBuilder<>(name, fqs);
    }

    /// Start the flow
    ///
    /// @return True if flow started, false if already started
    boolean start();

    /// Feed a single source item
    ///
    /// @param item Item, may not be null
    /// @return Number of items fed so far
    default long feed(T item) {
        return feed(List.of(Objects.requireNonNull(item, "item")));
    }

    /// Feed the source items
    ///
    /// @param items Items, may be null but may not contain null
    /// @return Number of items fed so far
    /// @throws NullPointerException if any item is null
    long feed(List<T> items);

    /// Stream the items and complete the run. Equivalent to:
    /// * [#start()]
    /// * [#feed(List)]
    /// * [#run()]
    ///
    /// @param items Items stream
    /// @return Completed run
    Run feed(Stream<T> items);

    /// Complete the run
    ///
    /// @return Completed run
    Run run();

    interface Run {

        default Run join() {
            return this;
        }

        long count();
    }

    interface Processor<T> {

        default Processor<T> andThen(Processor<T> next) {
            return items -> next.process(process(items));
        }

        Entries<T> process(Entries<T> items);
    }

    interface ErrorHandler<T> {

        T failed(Flow<T> flow, T item, Exception e);
    }

    interface Builder<T> {

        default To<T> fromSource() {
            return from((Name) null);
        }

        default With<T> fromSource(String to) {
            return fromSource(() -> to);
        }

        default With<T> fromSource(Name to) {
            return fromSource().to(to);
        }

        default With<T> from(String from, String to) {
            return from(() -> from).to(() -> to);
        }

        default With<T> from(Name from, Name to) {
            return from(from).to(to);
        }

        default To<T> from(String name) {
            return from(() -> name);
        }

        To<T> from(Name name);

        Builder<T> timeout(Duration timeout);

        Builder<T> onException(ErrorHandler<T> errorHandler);

        Builder<T> batchSize(int batchSize);

        FqFlows<T> build();

        interface To<T> {

            default With<T> to(String name) {
                return to(() -> name);
            }

            With<T> to(Name name);
        }

        interface With<T> {

            Builder<T> with(Processor<T> processor);
        }
    }
}
