package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.Handler;
import com.github.kjetilv.uplift.edam.Handling;
import com.github.kjetilv.uplift.edam.Info;
import com.github.kjetilv.uplift.hash.HashKind;

import static java.util.Objects.requireNonNull;

final class DefaultHandler<T, P extends Info<T, K>, K extends HashKind<K>>
    implements Handler<T, P, K> {

    private final Analyzer<T, K> analyzer;

    private final InfoProvider<T, P, K> infoProvider;

    DefaultHandler(Analyzer<T, K> analyzer, InfoProvider<T, P, K> infoProvider) {
        this.analyzer = requireNonNull(analyzer, "analyzer");
        this.infoProvider = requireNonNull(infoProvider, "memory");
    }

    @Override
    public Handling<T, P, K> handle(T item) {
        var analysis = analyzer.analyze(item);
        var metadata = infoProvider.build(item, analysis.trigger());
        return new Handling<>(analysis, metadata);
    }
}