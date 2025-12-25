package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.Fqs;

import java.time.Duration;
import java.util.stream.Stream;

public interface FqFlows<T> {

    static <T> Builder<T> builder(Name name, Fqs<T> fqs) {
        return new DefaultBuilder<>(name, fqs);
    }

    Run feed(Stream<T> items);

    Run feed();

    interface Run {

        long count();

        Run join();
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
            return from(null);
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

        To<T> from(Name name);

        Builder<T> timeout(Duration timeout);

        Builder<T> onException(ErrorHandler<T> errorHandler);

        Builder<T> batchSize(int batchSize);

        FqFlows<T> build();

        interface To<T> {

            With<T> to(Name name);
        }

        interface With<T> {

            Builder<T> with(Processor<T> processor);
        }
    }
}
