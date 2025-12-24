package com.github.kjetilv.uplift.fq.flows;

import java.util.List;

public interface Processor<T> {

    default T process(T t) {
        return process(List.of(t)).getFirst();
    }

    List<T> process(List<T> items);

    default Processor<T> andThen(Processor<T> next) {
        return items -> next.process(process(items));
    }
}
