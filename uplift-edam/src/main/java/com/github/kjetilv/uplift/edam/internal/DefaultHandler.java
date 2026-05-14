package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.Handler;
import com.github.kjetilv.uplift.edam.Handling;
import com.github.kjetilv.uplift.edam.Info;
import com.github.kjetilv.uplift.hash.HashKind;

import static java.util.Objects.requireNonNull;

final class DefaultHandler<T, P extends Info<T, H>, H extends HashKind<H>>
    implements Handler<T, P, H> {

    private final Analyzer<T, H> analyzer;

    private final InfoProvider<T, P, H> infoProvider;

    DefaultHandler(Analyzer<T, H> analyzer, InfoProvider<T, P, H> infoProvider) {
        this.analyzer = requireNonNull(analyzer, "analyzer");
        this.infoProvider = requireNonNull(infoProvider, "memory");
    }

    @Override
    public Handling<T, P, H> handling(T item) {
        var analysis = analyzer.analyze(item);
        var metadata = infoProvider.build(item, analysis.trigger());
        return new Handling<>(analysis, metadata);
    }
}
