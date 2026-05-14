package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.Analysis;
import com.github.kjetilv.uplift.edam.Analysis.Single;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.edam.patterns.PatternMatch;
import com.github.kjetilv.uplift.edam.patterns.PatternOccurrence;
import com.github.kjetilv.uplift.hash.HashKind;

import static com.github.kjetilv.uplift.edam.Analysis.Patterns;
import static com.github.kjetilv.uplift.edam.Analysis.Repeats;
import static java.util.Objects.requireNonNull;

record SequenceTracker<H extends HashKind<H>>(Storage<H> storage, Detector detector) {

    SequenceTracker {
        requireNonNull(storage, "storage");
    }

    SequenceTracker<H> update(Occurrence<H> occurrence) {
        storage.store(List.of(occurrence));
        return this;
    }

    Analysis<H> process(Occurrence<H> occurrence) {
        var matches = getPatternMatches(occurrence);
        return matches.isEmpty() ? new Single<>(occurrence)
            : isRepeated(matches) ? new Repeats<>(simpleMatches(matches))
                : new Patterns<>(occurrence, matches);
    }

    private List<PatternMatch<H>> getPatternMatches(Occurrence<H> occurrence) {
        return Progressions.repeats(
            occurrence,
            detector.patterns(storage, storage.count()),
            storage.forward().spool()
        );
    }

    private static boolean isRepeated(List<? extends PatternMatch<?>> repeats) {
        return repeats.size() == 1 && repeats.getFirst().isSimple();
    }

    private static <K extends HashKind<K>> List<Occurrence<K>> simpleMatches(
        List<PatternMatch<K>> matches
    ) {
        return matches.getFirst().occurrences()
            .stream()
            .map(PatternOccurrence::occurrences)
            .flatMap(Collection::stream)
            .sorted()
            .toList();
    }
}
