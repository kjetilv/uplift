package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module uplift.edam;
import module uplift.hash;

record Progressions<K extends HashKind<K>>(
    Occurrence<K> current,
    Map<Hash<K>, List<HashPattern<K>>> starters,
    List<PatternOccurrence<K>> progressing,
    List<PatternOccurrence<K>> completed
) {
    static <K extends HashKind<K>> List<PatternMatch<K>> repeats(
        Occurrence<K> occurrence,
        List<HashPattern<K>> hashPatterns,
        Stream<Occurrence<K>> occurrences
    ) {
        return occurrences.reduce(
            new Progressions<>(occurrence, hashPatterns),
            Progressions::proceed,
            Utils.Lists.noCombine()
        ).resolve();
    }

    Progressions {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(starters, "starters");
        Objects.requireNonNull(progressing, "progressing");
        Objects.requireNonNull(completed, "completed");
    }

    private Progressions(Occurrence<K> occurrence, List<HashPattern<K>> hashPatterns) {
        this(
            Objects.requireNonNull(occurrence, "occurrence"),
            group(Objects.requireNonNull(hashPatterns, "patterns")),
            Collections.emptyList(),
            Collections.emptyList()
        );
    }

    private Progressions<K> proceed(Occurrence<K> historical) {
        Hash<K> historicalHash = historical.hash();
        Hash<K> currentHash = current.hash();

        List<PatternOccurrence<K>> stillViableProgressions =
            progressing.stream()
                .map(patternOccurrence ->
                    patternOccurrence.matchingOccurrence(historical))
                .flatMap(Optional::stream)
                .toList();

        Collection<HashPattern<K>> stillViableHashPatterns = stillViableProgressions.stream()
            .map(PatternOccurrence::hashPattern)
            .collect(Collectors.toSet());

        List<PatternOccurrence<K>> newCandidates = starters.get(historicalHash)
            .stream()
            .filter(hashPattern ->
                hashPattern.isCandidate(historicalHash, currentHash) &&
                !stillViableHashPatterns.contains(hashPattern))
            .map(PatternOccurrence::new)
            .map(patternOccurrence ->
                patternOccurrence.matchingOccurrence(historical))
            .flatMap(Optional::stream)
            .toList();

        List<PatternOccurrence<K>> progressCompleted = Stream.of(stillViableProgressions, newCandidates)
            .flatMap(Collection::stream)
            .filter(PatternOccurrence::match)
            .toList();

        List<PatternOccurrence<K>> stillProgressing = Stream.of(stillViableProgressions, newCandidates)
            .flatMap(Collection::stream)
            .filter(((Predicate<PatternOccurrence<K>>) PatternOccurrence::match).negate())
            .toList();

        List<PatternOccurrence<K>> completed = Stream.concat(
                completed().stream(),
                progressCompleted.stream()
            )
            .toList();

        return new Progressions<>(current, starters, stillProgressing, completed);
    }

    private List<PatternMatch<K>> resolve() {
        return completed.stream()
            .collect(Collectors.groupingBy(PatternOccurrence::hashPattern))
            .entrySet()
            .stream()
            .filter(entry ->
                entry.getValue().size() > 1)
            .map(Progressions::patternOccurrences)
            .peek(Progressions::checkMatch)
            .toList();
    }

    private static <K extends HashKind<K>> void checkMatch(PatternMatch<K> patternMatch) {
        if (!patternMatch.match()) {
            throw new IllegalStateException("Invariant failed, not a complete match: " + patternMatch);
        }
    }

    private static <K extends HashKind<K>> Map<Hash<K>, List<HashPattern<K>>> group(List<HashPattern<K>> hashPatterns) {
        return hashPatterns.stream()
            .collect(Collectors.groupingBy(pattern -> pattern.hashes().getFirst()));
    }

    private static <K extends HashKind<K>> PatternMatch<K> patternOccurrences(Map.Entry<HashPattern<K>, List<PatternOccurrence<K>>> entry) {
        return new PatternMatch<>(entry.getKey(), entry.getValue());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + current.hash() + ":" +
               " progressing: " + progressing +
               " completed: " + completed +
               "]";
    }
}
