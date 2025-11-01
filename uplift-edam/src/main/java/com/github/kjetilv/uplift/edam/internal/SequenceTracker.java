package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.Analysis;
import com.github.kjetilv.uplift.edam.Analysis.None;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.edam.patterns.PatternMatch;
import com.github.kjetilv.uplift.edam.patterns.PatternOccurrence;
import com.github.kjetilv.uplift.hash.HashKind;

import static com.github.kjetilv.uplift.edam.Analysis.Multiple;
import static com.github.kjetilv.uplift.edam.Analysis.Simple;
import static java.util.Objects.requireNonNull;

record SequenceTracker<K extends HashKind<K>>(Storage<K> storage, Detector detector) {

    SequenceTracker {
        requireNonNull(storage, "storage");
    }

    SequenceTracker<K> update(Occurrence<K> occurrence) {
        storage.store(List.of(occurrence));
        return this;
    }

    Analysis<K> process(Occurrence<K> occurrence) {
        var matches = getPatternMatches(occurrence);
        return matches.isEmpty() ? new None<>(occurrence)
            : isSimple(matches) ? new Simple<>(simpleMatches(matches))
                : new Multiple<>(occurrence, matches);
    }

    private List<PatternMatch<K>> getPatternMatches(Occurrence<K> occurrence) {
        return Progressions.repeats(
            occurrence,
            detector.patterns(storage, storage.count()),
            storage.forward().spool()
        );
    }

    private static boolean isSimple(List<? extends PatternMatch<?>> repeats) {
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
