package com.github.kjetilv.uplift.fq.paths;

public interface Factories<S, I> {

    Puller<I> puller(S path);

    Writer<I> writer(S path);
}
