package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module uplift.hash;
import com.github.kjetilv.uplift.edam.Analysis;
import com.github.kjetilv.uplift.edam.Analysis.None;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.edam.patterns.Pattern;
import com.github.kjetilv.uplift.edam.patterns.PatternMatch;
import com.github.kjetilv.uplift.edam.patterns.PatternOccurrence;

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
        List<PatternMatch<K>> matches = getPatternMatches(occurrence);
        return matches.isEmpty() ? new None<>(occurrence)
            : isSimple(matches) ? new Simple<>(simpleMatches(matches))
                : new Multiple<>(occurrence, matches);
    }

    private List<PatternMatch<K>> getPatternMatches(Occurrence<K> occurrence) {
        List<Pattern<K>> patterns = detector.patterns(storage, storage.count());
        Stream<Occurrence<K>> occurrenceStream = storage.forward().spool();
        return Progressions.repeats(
            occurrence,
            patterns,
            occurrenceStream
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
