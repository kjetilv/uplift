package com.github.kjetilv.uplift.edam.patterns;

import com.github.kjetilv.uplift.edam.internal.Utils;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @param pattern     A pattern
 * @param occurrences Actual occurrences following the pattern
 */
@SuppressWarnings("unused")
public record PatternOccurrence<K extends HashKind<K>>(Pattern<K> pattern, List<Occurrence<K>> occurrences)
    implements Spanning, Comparable<PatternOccurrence<K>>, Iterable<Occurrence<K>> {

    public PatternOccurrence(Pattern<K> pattern) {
        this(pattern, new ArrayList<>());
    }

    public PatternOccurrence(Pattern<K> pattern, List<Occurrence<K>> occurrences) {
        if (pattern.validOccurrences(occurrences)) {
            this.pattern = requireNonNull(pattern, "pattern");
            this.occurrences = Utils.Lists.nonNullSorted(occurrences, "occurrences");
        } else {
            throw new IllegalArgumentException(pattern + ": Not an occurrence: " + occurrences);
        }
    }

    public Timespan timespan() {
        return Timespan.of(occurrences);
    }

    @Override
    public int compareTo(PatternOccurrence other) {
        return timespan().compareTo(other.timespan());
    }

    public String toStringBody() {
        Occurrence<K> firstTime = occurrences.getFirst();
        Utils.Unit unit = Utils.Unit.of(timespan().duration());
        String tail = occurrences.stream().skip(1)
            .map(occurrence ->
                occurrence.hash().toShortString() + "+" +
                unit.print(Duration.between(firstTime.time(), occurrence.time()))
            )
            .collect(Collectors.joining(" "));
        String head = firstTime.hash().toShortString() + "@" + print(firstTime.time());
        return "<" + head + " " + tail + ">";
    }

    public Optional<PatternOccurrence<K>> matchingOccurrence(Occurrence<K> occ) {
        return matchesNext(occ.hash())
            ? Optional.of(new PatternOccurrence<>(pattern, add(occ)))
            : Optional.empty();
    }

    public boolean matchesNext(Hash<K> hash) {
        return occurrences.size() < pattern().length() && pattern.hash(occurrences().size()).equals(hash);
    }

    public boolean match() {
        return pattern.length() == occurrences.size();
    }

    public boolean isSimple() {
        return pattern.isSimple();
    }

    @Override
    public Iterator<Occurrence<K>> iterator() {
        return occurrences.iterator();
    }

    private List<Occurrence<K>> add(Occurrence<K> occ) {
        return Stream.concat(occurrences.stream(), Stream.of(occ))
            .toList();
    }

    private static String print(Instant time) {
        return time.truncatedTo(ChronoUnit.MILLIS)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_TIME);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + pattern + ": " + occurrences + "]";
    }
}
