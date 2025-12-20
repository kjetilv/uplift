package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.paths.Puller;
import com.github.kjetilv.uplift.fq.paths.Writer;

public interface AccessProvider<S, I> {

    Puller<I> puller(S path);

    Writer<I> writer(S path);

    Tombstone<S> tombstone(S path);
}
