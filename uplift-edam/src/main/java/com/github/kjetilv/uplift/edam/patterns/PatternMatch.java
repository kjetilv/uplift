package com.github.kjetilv.uplift.edam.patterns;

import com.github.kjetilv.uplift.edam.internal.Utils;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/// @param pattern     The pattern that has occurred
/// @param occurrences THe occurrences
/// @param timespan    Timespan of all occurrences
public record PatternMatch<K extends HashKind<K>>(
    Pattern<K> pattern,
    List<PatternOccurrence<K>> occurrences,
    Timespan timespan
) implements Temporal, Comparable<PatternMatch<K>>, Iterable<PatternOccurrence<K>> {

    public static <K extends HashKind<K>>String toShortString(List<PatternMatch<K>> sequenceOccurrences) {
        return toShortString(sequenceOccurrences, null);
    }

    public static <K extends HashKind<K>> String toShortString(
        Collection<PatternMatch<K>> sequenceOccurrences,
        String delimiter
    ) {
        return sequenceOccurrences.stream()
            .sorted(Comparator.comparing(PatternMatch::pattern))
            .map(PatternMatch::toShortString)
            .map(Object::toString)
            .collect(Collectors.joining(delimiter == null ? "⁄" : delimiter));
    }

    public PatternMatch(Pattern<K> pattern, List<PatternOccurrence<K>> patternOccurrences) {
        this(
            requireNonNull(pattern, "pattern"),
            Utils.Lists.nonNullSorted(patternOccurrences, "patternOccurrences"),
            Timespan.of(patternOccurrences)
        );
    }

    public PatternMatch {
        requireNonNull(pattern, "pattern");
        requireNonNull(occurrences, "occurrences");
        requireNonNull(timespan, "timespan");
    }

    public int count(Hash<K> hash) {
        return pattern.count(hash) * occurrences.size();
    }

    public boolean isSimple() {
        return pattern.isSimple();
    }

    public boolean isCombined() {
        return pattern.isCombined();
    }

    @Override
    public Instant startTime() {
        return timespan().start();
    }

    public Instant lastTime() {
        return timespan.end();
    }

    public Occurrence<K> firstOccurrence() {
        return occurrences.getFirst().occurrences().getFirst();
    }

    @Override
    public int compareTo(PatternMatch other) {
        return timespan().compareTo(other.timespan());
    }

    @Override
    public Iterator<PatternOccurrence<K>> iterator() {
        return occurrences.iterator();
    }

    public boolean match() {
        return occurrences.stream().allMatch(PatternOccurrence::match);
    }

    public String toShortString() {
        return occurrences.size() + "x" + pattern.toShortString() ;
    }

    public Optional<List<Occurrence<K>>> singleOccurrence() {
        return isSimple()
            ? Optional.ofNullable(occurrences().getFirst().occurrences())
            : Optional.empty();
    }

    @Override
    public String toString() {
        if (pattern.length() == 1 && occurrences.size() == 1) {
            return getClass().getSimpleName() + "[" +
                   pattern.hashes().getFirst() + "@" + occurrences.getFirst() +
                   "]";
        }
        return getClass().getSimpleName() + "[" +
               "/" + pattern.hashes().size() +
               "<" + pattern.hashes()
                   .stream()
                   .map(Hash::toString)
                   .collect(Collectors.joining()) +
               ">" +
               " /" + occurrences.size() +
               "<" + occurrences.stream()
                   .map(PatternOccurrence::toStringBody)
                   .collect(Collectors.joining(" ")) +
               ">]";
    }
}
